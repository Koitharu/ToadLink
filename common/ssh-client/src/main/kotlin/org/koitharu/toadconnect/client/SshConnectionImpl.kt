package org.koitharu.toadconnect.client

import com.trilead.ssh2.ChannelCondition
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koitharu.toadlink.core.DeviceDescriptor
import java.io.ByteArrayOutputStream
import java.io.Closeable

internal class SshConnectionImpl(
    override val deviceDescriptor: DeviceDescriptor,
    private val connection: Connection,
) : SshConnection, ConnectionMonitor, Closeable {

    val isConnected = MutableStateFlow(true)
    private val mutex = Mutex()

    override fun connectionLost(reason: Throwable?) {
        reason?.printStackTrace()
        isConnected.value = false
    }

    override fun close() {
        Dispatchers.IO.dispatch(NonCancellable) {
            connection.close()
        }
    }

    override suspend fun execute(cmdline: String): String {
        resurrectConnection()
        return runInterruptible(Dispatchers.IO) {
            connection.openSession().use { session ->
                session.execCommand(cmdline)
                session.waitForCondition(ChannelCondition.EXIT_STATUS, 5_000L)
                if (session.exitStatus == 0) {
                    session.stdout.bufferedReader().use { it.readText() }.trim()
                } else {
                    val errMsg =
                        session.stderr.bufferedReader().use { it.readText() }.trim()
                            .ifEmpty { null }
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
        connection.openSession().use { session ->
            session.execCommand(cmdline)
            invokeOnClose {
                session.close()
            }
            session.stdout.bufferedReader().use {
                for (line in it.lineSequence()) {
                    val result = trySend(line.trim())
                    if (result.isClosed) {
                        break
                    }
                }
            }
            if (session.exitStatus == 0) {
                close()
            } else {
                val errMsg =
                    session.stderr.bufferedReader().use { it.readText() }.trim().ifEmpty { null }
                close(
                    RemoteProcessException(
                        exitCode = session.exitStatus,
                        message = errMsg,
                    )
                )
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getFileContent(path: String): ByteArray =
        runInterruptible(Dispatchers.IO) {
            val client = connection.createSCPClient()
            val output = ByteArrayOutputStream()
            client.get(path, output)
            output.toByteArray()
        }

    private suspend fun resurrectConnection() {
        if (!isConnected.value) {
            mutex.withLock {
                if (!isConnected.value) {
                    runInterruptible(Dispatchers.IO) {
                        connection.connect()
                        connection.authenticateWithPassword(deviceDescriptor.username, deviceDescriptor.password)
                        isConnected.value = true
                    }
                }
            }
        }
    }
}