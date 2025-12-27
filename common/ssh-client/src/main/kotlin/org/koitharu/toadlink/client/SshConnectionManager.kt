package org.koitharu.toadlink.client

import com.trilead.ssh2.Connection
import com.trilead.ssh2.log.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.util.firstNotNull
import org.koitharu.toadlink.core.util.runCatchingCancellable

class SshConnectionManager(
    private val coroutineScope: CoroutineScope,
) {

    init {
        coroutineScope.launch {
            try {
                awaitCancellation()
            } finally {
                disconnect()
            }
        }
    }

    private val _activeConnection = MutableStateFlow<SshConnectionImpl?>(null)

    val activeConnection: StateFlow<SshConnection?>
        get() = _activeConnection.asStateFlow()

    suspend fun awaitConnection(): SshConnection = _activeConnection.firstNotNull()

    fun connect(
        deviceDescriptor: DeviceDescriptor,
    ): Deferred<Result<SshConnection>> {
        activeConnection.value?.let {
            if (it.host == deviceDescriptor) {
                return CompletableDeferred(Result.success(it))
            }
        }
        return coroutineScope.async {
            runCatchingCancellable {
                val connection = Connection(deviceDescriptor.hostname, deviceDescriptor.port)
                val sshConnection = SshConnectionImpl(deviceDescriptor, connection)
                connection.addConnectionMonitor(sshConnection)
                observeConnection(sshConnection)
                runInterruptible(Dispatchers.IO) {
                    connection.connect()
                    connection.authenticateWithPassword(
                        deviceDescriptor.username,
                        deviceDescriptor.password
                    )
                }
                _activeConnection.getAndUpdate { sshConnection }?.close()
                sshConnection
            }
        }
    }

    fun disconnect() {
        _activeConnection.getAndUpdate { null }?.close()
    }

    private fun observeConnection(connection: SshConnectionImpl) = coroutineScope.launch {
        connection.isConnected.collect { connected ->
            if (!connected) {
                _activeConnection.compareAndSet(connection, null)
            }
        }
    }

    companion object {

        fun setLoggingEnabled(isEnabled: Boolean) {
            Logger.enabled = isEnabled
        }

        suspend fun getHostKey(
            hostname: String,
            port: Int,
        ): ByteArray = runInterruptible(Dispatchers.IO) {
            val connection = Connection(hostname, port)
            connection.connect()
            connection.connectionInfo.serverHostKey
        }
    }
}