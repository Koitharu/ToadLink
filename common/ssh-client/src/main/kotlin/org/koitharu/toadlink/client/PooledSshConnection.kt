package org.koitharu.toadlink.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Source
import org.connectbot.sshlib.SftpClient
import org.connectbot.sshlib.getOrThrow
import org.koitharu.toadlink.client.fs.SshFileSystem
import org.koitharu.toadlink.client.fs.withExtraCloseable
import org.koitharu.toadlink.core.DeviceDescriptor
import java.util.Collections
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

internal class PooledSshConnection(
    override val host: DeviceDescriptor,
    private val logger: Logger,
    parentScope: CoroutineScope,
) : SshConnection {

    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )
    private val activeConnections = Collections.synchronizedSet(HashSet<DirectSshConnection>())
    private val mutex = Mutex()

    override val coroutineContext: CoroutineContext
        get() = scope.coroutineContext

    override val fileSystem: FileSystem
        get() = SshFileSystem(this)

    override suspend fun execute(cmdline: String): String {
        val connection = createConnection()
        return try {
            connection.execute(cmdline)
        } finally {
            withContext(NonCancellable) {
                removeConnection(connection)
            }
        }
    }

    override suspend fun executeAsSource(cmdline: String): Source {
        val connection = createConnection()
        return try {
            connection.executeAsSource(cmdline)
                .withExtraCloseable {
                    runBlocking { removeConnection(connection) }
                }
        } catch (e: Throwable) {
            withContext(NonCancellable) {
                removeConnection(connection)
            }
            throw e
        }
    }

    override fun executeContinuously(cmdline: String): Flow<String> = flow {
        val connection = createConnection()
        try {
            connection.executeContinuously(cmdline).collect {
                emit(it)
            }
        } finally {
            withContext(NonCancellable) {
                removeConnection(connection)
            }
        }
    }

    suspend fun getSftpClient(): SftpClient {
        val connection = DirectSshConnection(host, host.createClient(), logger, scope)
        connection.connect()
        return try {
            connection.client.openSftp().getOrThrow()
        } catch (e: Throwable) {
            withContext(NonCancellable) {
                connection.close()
            }
            throw e
        }
    }


    suspend fun close() {
        scope.cancel()
        mutex.withLock {
            activeConnections.forEach {
                it.close()
            }
            activeConnections.clear()
        }
    }

    private suspend fun createConnection(): DirectSshConnection {
        val connection = mutex.withLock {
            val connection = DirectSshConnection(host, host.createClient(), logger, scope)
            activeConnections.add(connection)
            connection
        }
        try {
            connection.connect()
            return connection
        } catch (e: Throwable) {
            withContext(NonCancellable) {
                removeConnection(connection)
            }
            throw e
        }
    }

    private suspend fun removeConnection(connection: DirectSshConnection) = mutex.withLock {
        activeConnections.remove(connection)
        connection.close()
    }
}