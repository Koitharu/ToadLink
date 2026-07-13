package org.koitharu.toadlink.actions.ui.list

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koitharu.toadlink.actions.ui.ExecutionState
import org.koitharu.toadlink.actions.ui.list.ActionsIntent.Execute
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.storage.RemoteActionsRepository
import org.koitharu.toadlink.ui.mvi.MviViewModel

@HiltViewModel(assistedFactory = ActionsViewModel.Factory::class)
internal class ActionsViewModel @AssistedInject constructor(
    @Assisted device: DeviceDescriptor,
    private val connectionManager: SshConnectionManager,
    private val repository: RemoteActionsRepository,
) : MviViewModel<ActionsState, ActionsIntent, ActionsEffect>(ActionsState(device)) {

    private val executionState = MutableStateFlow<PersistentMap<Int, ExecutionState>>(
        persistentMapOf()
    )

    init {
        viewModelScope.launch {
            combine(
                repository.observeAll(device.id),
                executionState
            ) { actions, execution ->
                actions.map { action ->
                    val state = execution[action.id]
                    ActionItem(
                        host = this@ActionsViewModel.state.value.host,
                        action = action,
                        state = state ?: ExecutionState.None,
                    )
                }
            }.collect {
                state.value = ActionsState(
                    host = state.value.host,
                    actions = it.toPersistentList(),
                    isLoading = false,
                )
            }
        }
    }

    override fun handleIntent(intent: ActionsIntent) = when (intent) {
        is Execute -> executeAction(intent.action)
    }

    private fun executeAction(action: RemoteAction) {
        viewModelScope.launch(Dispatchers.Default) {
            executionState.update { it.putting(action.id, ExecutionState.Running) }
            runCatchingCancellable {
                connectionManager
                    .getConnection(state.value.host)
                    .execute(action.cmdline)
            }.onSuccess { result ->
                executionState.update { it.putting(action.id, ExecutionState.Success(result)) }
            }.onFailure { error ->
                executionState.update { it.putting(action.id, ExecutionState.Failed(error)) }
            }
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(device: DeviceDescriptor): ActionsViewModel
    }
}