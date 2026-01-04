package org.koitharu.toadlink.control

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.control.ControlState.Connected
import org.koitharu.toadlink.control.ControlState.Connecting
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.storage.DevicesRepository
import org.koitharu.toadlink.ui.mvi.MviViewModel

@HiltViewModel(assistedFactory = ControlViewModel.Factory::class)
class ControlViewModel @AssistedInject constructor(
    @Assisted private val deviceId: Int,
    private val devicesRepository: DevicesRepository,
    private val connectionManager: SshConnectionManager,
) : MviViewModel<ControlState, ControlIntent, ControlEffect>(Connecting(null)) {

    override fun handleIntent(intent: ControlIntent) = when (intent) {
        is ControlIntent.SwitchSection -> state.update {
            (it as? Connected)?.copy(section = intent.section) ?: it
        }
    }

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val device = runCatchingCancellable {
                val device = devicesRepository.get(deviceId)
                connectionManager.connect(device).await().getOrThrow()
                device
            }.onSuccess { device ->
                state.value = Connected(device, ControlSection.ACTIONS)
            }.onFailure { error ->
                state.value = ControlState.Error(null, error)
            }.getOrNull() ?: return@launch
            connectionManager.activeConnection.first { it == null }
            sendEffect(ControlEffect.CloseScreen)
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(deviceId: Int): ControlViewModel
    }
}