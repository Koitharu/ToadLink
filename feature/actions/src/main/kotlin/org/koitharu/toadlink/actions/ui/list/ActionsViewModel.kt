package org.koitharu.toadlink.actions.ui.list

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koitharu.toadconnect.client.SshConnectionManager
import org.koitharu.toadlink.storage.RemoteActionsRepository
import org.koitharu.toadlink.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class ActionsViewModel @Inject constructor(
    private val connectionManager: SshConnectionManager,
    private val repository: RemoteActionsRepository,
) : MviViewModel<ActionsState, ActionsIntent, ActionsEffect>(ActionsState()) {

    init {
        viewModelScope.launch {
            connectionManager.activeConnection.flatMapLatest {
                if (it == null) {
                    emptyFlow()
                } else {
                    val deviceId = it.deviceDescriptor.id
                    repository.observeAll(deviceId)
                }
            }.collect {
                state.value = ActionsState(
                    actions = it,
                    isLoading = false,
                )
            }
        }
    }

    override fun handleIntent(intent: ActionsIntent) {
        TODO("Not yet implemented")
    }
}