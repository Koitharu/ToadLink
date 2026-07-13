package org.koitharu.toadlink.client

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koitharu.toadlink.core.DeviceDescriptor
import java.util.logging.Logger

public class SshConnectionManager(
    private val coroutineScope: CoroutineScope,
) {

    private val logger = Logger.getLogger("TSSH")
    private val mutableConnections =
        MutableStateFlow(persistentMapOf<DeviceDescriptor, PooledSshConnection>())
    private val connectionMutex = Mutex()

    public val connections: StateFlow<ImmutableMap<DeviceDescriptor, SshConnection>> =
        mutableConnections.asStateFlow()

    public fun observeConnection(host: DeviceDescriptor): Flow<SshConnection?> = connections.map {
        it[host]
    }.distinctUntilChanged()

    public suspend fun getConnection(host: DeviceDescriptor): SshConnection =
        connectionMutex.withLock {
            mutableConnections.value[host]?.let {
                return@withLock it
            }
            val connection = PooledSshConnection(host, logger, coroutineScope)
            mutableConnections.update { it.putting(host, connection) }
            logger.info("Connected: ${host.displayName}")
            connection
        }

    public fun peekConnection(host: String): SshConnection? {
        return mutableConnections.value.entries.find { it.key.address == host }?.value
    }

    public fun peekConnection(deviceId: Int): SshConnection? {
        return mutableConnections.value.entries.find { it.key.id == deviceId }?.value
    }

    public suspend fun disconnect(host: DeviceDescriptor) {
        val connection = connectionMutex.withLock {
            val snapshot = mutableConnections.value
            val connection = snapshot[host] ?: return
            mutableConnections.value = snapshot.removing(host)
            connection
        }
        logger.info("Disconnected: ${host.displayName}")
        withContext(NonCancellable) {
            connection.close()
        }
    }

    public suspend fun disconnect(deviceId: Int) {
        mutableConnections.value.keys.find {
            it.id == deviceId
        }?.let {
            disconnect(it)
        }
    }

    public suspend fun testConnection(host: DeviceDescriptor) {
        val connection = DirectSshConnection(host, host.createClient(), logger, coroutineScope)
        try {
            connection.connect()
            connection.ping()
        } finally {
            withContext(NonCancellable) {
                connection.close()
            }
        }
    }
}