package org.koitharu.toadlink.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import okio.FileSystem
import okio.Source
import org.connectbot.sshlib.ConnectResult
import org.connectbot.sshlib.PingResult
import org.connectbot.sshlib.SshClient
import org.connectbot.sshlib.SshException
import org.connectbot.sshlib.SshSession
import org.koitharu.toadlink.client.exceptions.RemoteProcessException
import org.koitharu.toadlink.client.fs.AsyncChannelSource
import org.koitharu.toadlink.client.fs.withExtraCloseable
import org.koitharu.toadlink.core.DeviceDescriptor
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

internal class DirectSshConnection(
    override val host: DeviceDescriptor,
    internal val client: SshClient,
    private val logger: Logger,
    parentScope: CoroutineScope,
) : SshConnection {

    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )
    private val mutex = Mutex()

    val isConnected: Boolean
        get() = client.isAuthenticated

    override val coroutineContext: CoroutineContext
        get() = scope.coroutineContext

    override val fileSystem: FileSystem
        get() = throw UnsupportedOperationException("${javaClass.simpleName} cannot instantiate a FileSystem")

    override suspend fun execute(cmdline: String): String {
        return requireSession().use { session ->
            logger.info("<< $cmdline")
            check(session.requestExec(cmdline)) {
                "Unable to execute $cmdline"
            }
            session.readStdout()
        }
    }

    override suspend fun executeAsSource(cmdline: String): Source {
        val session = requireSession()
        try {
            check(session.requestExec(cmdline)) {
                "Unable to execute $cmdline"
            }
            session.readError()?.let {
                throw it
            }
            return AsyncChannelSource(session.stdout).withExtraCloseable(session)
        } catch (e: Throwable) {
            session.close()
            throw e
        }
    }

    override fun executeContinuously(cmdline: String): Flow<String> = flow {
        requireSession().use { session ->
            check(session.requestExec(cmdline)) {
                "Unable to execute $cmdline"
            }
            session.stdout.consumeAsFlow().catch { e ->
                val error = session.readError()
                if (error != null) {
                    error.addSuppressed(e)
                    throw error
                } else {
                    throw e
                }
            }.collect {
                emit(it.toString(Charsets.UTF_8).trim())
            }
        }
    }

    internal suspend fun connect() = mutex.withLock {
        when (val result = client.connect()) {
            ConnectResult.Success -> client.authenticate(host).ensureSuccess()

            is ConnectResult.AlgorithmMismatch -> error("Algorithm mismatch: ${result.message}")
            is ConnectResult.HostKeyRejected -> error("Host key rejected")
            is ConnectResult.ProtocolError -> error("Protocol error: ${result.message}")
            is ConnectResult.TransportError -> error("Transport error: ${result.cause.message}")
        }
    }

    suspend fun ping() = when (val result = client.ping()) {
        is PingResult.Failure -> throw SshException(
            result.cause.message ?: "Ping failed",
            result.cause
        )

        PingResult.NotAuthenticated -> throw SshException("Not authenticated")
        PingResult.NotSupported -> throw SshException("Ping is not supported")
        is PingResult.Success -> Unit
    }

    internal suspend fun close() {
        scope.cancel()
        mutex.withLock {
            client.disconnect()
        }
    }

    private suspend fun requireSession(): SshSession = withTimeout(3.seconds) {
        client.openSession() ?: error("Unable to open session")
    }

    private suspend fun SshSession.readStdout(): String {
        try {
            return stdout.receive().toString(Charsets.UTF_8).trim()
        } catch (e: Exception) {
            val (code, extended) = readExtended() ?: run {
                if (e is ClosedReceiveChannelException) {
                    return ""
                } else {
                    throw e
                }
            }
            if (code == 0) {
                return extended.toString(Charsets.UTF_8).trim()
            } else {
                val message = stderr.tryReceive().getOrNull()?.toString(Charsets.UTF_8)?.trim()
                throw RemoteProcessException(code, message).also {
                    it.addSuppressed(e)
                }
            }
        }
    }

    private fun SshSession.readError(): RemoteProcessException? {
        val message = stderr.tryReceive().getOrNull()?.toString(Charsets.UTF_8)?.trim()
        return if (!message.isNullOrEmpty()) {
            RemoteProcessException(
                exitCode = -1,
                message = message
            )
        } else {
            null
        }
    }
}