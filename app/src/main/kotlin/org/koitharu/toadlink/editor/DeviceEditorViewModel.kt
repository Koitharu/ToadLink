package org.koitharu.toadlink.editor

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.editor.DeviceEditorIntent.SaveDevice
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdateHostname
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdatePassword
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdatePort
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdateUsername
import org.koitharu.toadlink.storage.DevicesRepository
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviViewModel

@HiltViewModel(assistedFactory = DeviceEditorViewModel.Factory::class)
class DeviceEditorViewModel @AssistedInject constructor(
    @Assisted deviceId: Int,
    @Assisted initialAddress: String?,
    private val connectionManager: SshConnectionManager,
    private val storage: DevicesRepository,
) : MviViewModel<DeviceEditorState, DeviceEditorIntent, DeviceEditorEffect>(
    DeviceEditorState(initialAddress, deviceId == 0)
) {

    init {
        if (deviceId != 0) {
            viewModelScope.launch(Dispatchers.Default) {
                val device = runCatchingCancellable {
                    storage.get(deviceId)
                }.getOrNull()
                if (device == null) {
                    // TODO
                } else {
                    state.value = DeviceEditorState(device)
                }
            }
        }
    }

    override fun handleIntent(intent: DeviceEditorIntent) = when (intent) {
        is UpdateHostname -> state.update {
            it.copy(hostname = intent.value)
        }

        is UpdatePassword -> state.update {
            it.copy(password = intent.value)
        }

        is UpdatePort -> state.update {
            it.copy(port = intent.value)
        }

        is UpdateUsername -> state.update {
            it.copy(username = intent.value)
        }

        SaveDevice -> addDevice()
    }

    private fun addDevice() {
        state.value = state.value.validate()
        if (state.value.hasErrors()) {
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val snapshot = state.updateAndGet {
                it.copy(isLoading = true)
            }
            val data = DeviceDescriptor(
                id = 0,
                hostname = snapshot.hostname,
                port = snapshot.port,
                alias = null,
                username = snapshot.username,
                password = snapshot.password
            )
            storage.store(data)
            state.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun DeviceEditorState.validate() = copy(
        portError = when {
            port !in 1..65535 -> R.string.error_generic
            else -> 0
        }
    )

    @AssistedFactory
    interface Factory {

        fun create(
            deviceId: Int,
            initialAddress: String?,
        ): DeviceEditorViewModel
    }
}