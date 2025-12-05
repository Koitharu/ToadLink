package org.koitharu.toadlink.finddevice

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.finddevice.FindDeviceEffect.OnError
import org.koitharu.toadlink.finddevice.FindDeviceEffect.OpenDevice
import org.koitharu.toadlink.finddevice.FindDeviceIntent.Connect
import org.koitharu.toadlink.finddevice.FindDeviceIntent.Refresh
import org.koitharu.toadlink.network.NetworkScanner
import org.koitharu.toadlink.storage.DevicesRepository
import org.koitharu.toadlink.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
class FindDeviceViewModel @Inject constructor(
    private val networkScanner: NetworkScanner,
    private val devicesRepository: DevicesRepository,
    private val connectionManager: SshConnectionManager,
) : MviViewModel<FindDeviceState, FindDeviceIntent, FindDeviceEffect>(FindDeviceState()) {

    private var networkScanningJob: Job = scanLocalNetwork()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            devicesRepository.observeAll()
                .collect { savedDevices ->
                    state.update { it.copy(savedDevices = savedDevices) }
                }
        }
        viewModelScope.launch(Dispatchers.Default) {
            connectionManager.activeConnection.collect { connectedDevice ->
                state.update {
                    it.copy(
                        connectedDevice = connectedDevice?.deviceDescriptor?.id ?: 0
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: FindDeviceIntent) {
        when (intent) {
            is Connect -> connect(intent.device)
            Refresh -> {
                if (!networkScanningJob.isActive) {
                    networkScanningJob = scanLocalNetwork()
                }
            }
        }
    }

    private fun scanLocalNetwork() = viewModelScope.launch {
        networkScanner.observeLocalNetwork()
            .onStart { state.update { it.copy(isScanning = true) } }
            .onCompletion { state.update { it.copy(isScanning = false) } }
            .collect { availableDevices ->
                state.update {
                    it.copy(availableDevices = availableDevices)
                }
            }
    }

    private fun connect(device: DeviceDescriptor) {
        viewModelScope.launch {
            connectionManager.connect(device)
                .await()
                .onSuccess {
                    sendEffect(OpenDevice(it.deviceDescriptor.id))
                }.onFailure {
                    sendEffect(OnError(it))
                }
        }
    }
}