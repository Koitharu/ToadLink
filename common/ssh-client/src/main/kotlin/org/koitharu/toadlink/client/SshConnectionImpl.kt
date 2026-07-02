package org.koitharu.toadlink.client

import com.trilead.ssh2.ChannelCondition
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.FileSystem
import okio.Source
import okio.source
import org.koitharu.toadlink.client.fs.SshFileSystem
import org.koitharu.toadlink.core.DeviceDescriptor
import java.io.Closeable
import java.util.logging.Logger

internal class SshConnectionImpl(
    override val host: DeviceDescriptor,
    internal val connection: Connection,
) : SshConnection, ConnectionMonitor, Closeable {

    val isConnected = MutableStateFlow(true)
    private val mutex = Mutex()

    override val fileSystem: FileSystem
        get() = SshFileSystem(this)

    override fun connectionLost(reason: Throwable?) {
        reason?.printStackTrace()
        isConnected.value = false
    }

    override fun close() {
        Dispatchers.IO.dispatch(NonCancellable) {
            logd("Connection closed")
            connection.close()
        }
    }

    override suspend fun execute(cmdline: String): String {
        resurrectConnection()
        return runInterruptible(Dispatchers.IO) {
            connection.openSession().use { session ->
                logd("<< $cmdline")
                session.execCommand(String(cmdline.toByteArray(Charsets.ISO_8859_1)))
                session.waitForCondition(ChannelCondition.EXIT_STATUS, 5_000L)
                logd(">> $cmdline with code ${session.exitStatus}")
                if (session.exitStatus == 0) {
                    session.stdout.bufferedReader().use { it.readText() }.trim()
                } else {
                    val errMsg =
                        session.stderr.bufferedReader().use { it.readText() }.trim()
                            .ifEmpty { null }
                    logw("!! $errMsg")
                    throw RemoteProcessException(
                        exitCode = session.exitStatus,
                        message = errMsg,
                    )
                }
            }
        }
    }

    override suspend fun executeAsSource(cmdline: String): Source {
        resurrectConnection()
        return runInterruptible(Dispatchers.IO) {
            connection.openSession().use { session ->
                logd("<< $cmdline")
                session.execCommand(cmdline)
                session.waitForCondition(ChannelCondition.EXIT_STATUS, 5_000L)
                logd(">> $cmdline with code ${session.exitStatus}")
                if (session.exitStatus == 0) {
                    session.stdout.source()
                } else {
                    val errMsg =
                        session.stderr.bufferedReader().use { it.readText() }.trim()
                            .ifEmpty { null }
                    logw("!! $errMsg")
                    throw RemoteProcessException(
                        exitCode = session.exitStatus,
                        message = errMsg,
                    )
                }
            }
        }
    }

    override fun executeContinuously(cmdline: String): Flow<String> = channelFlow {
        resurrectConnection()
        logd("<-- $cmdline")
        connection.openSession().use { session ->
            var readerJob: Job? = null
            invokeOnClose {
                readerJob?.cancel()
            }
            readerJob = launch {
                runInterruptible(Dispatchers.IO) {
                    logd("<<< $cmdline")
                    session.execCommand(cmdline)
                    session.stdout.bufferedReader().use {
                        for (line in it.lineSequence()) {
                            logd(">>> $line")
                            val result = trySend(line.trim())
                            if (result.isClosed) {
                                break
                            }
                        }
                    }
                }
            }
            readerJob.join()
            logd(">>> $cmdline with code ${session.exitStatus}")
            if (session.exitStatus == 0) {
                close()
            } else {
                val errMsg =
                    session.stderr.bufferedReader().use { it.readText() }.trim().ifEmpty { null }
                logw("!!! $errMsg")
                close(
                    RemoteProcessException(
                        exitCode = session.exitStatus,
                        message = errMsg,
                    )
                )
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun resurrectConnection() {
        if (!isConnected.value) {
            mutex.withLock {
                if (!isConnected.value) {
                    runInterruptible(Dispatchers.IO) {
                        connection.connect()
                        connection.authenticateWithPassword(
                            host.username,
                            host.password
                        )
                        isConnected.value = true
                    }
                }
            }
        }
    }

    private fun logd(msg: String) {
        Logger.getLogger(TAG).info("${host.displayName} $msg")
    }

    private fun logw(msg: String) {
        Logger.getLogger(TAG).warning("${host.displayName} $msg")
    }

    private companion object {

        const val TAG = "TSSH"
    }
}