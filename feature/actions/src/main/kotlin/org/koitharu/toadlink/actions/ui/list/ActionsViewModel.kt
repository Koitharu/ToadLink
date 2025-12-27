package org.koitharu.toadlink.actions.ui.list

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koitharu.toadlink.actions.ui.ExecutionState
import org.koitharu.toadlink.actions.ui.list.ActionsIntent.Execute
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.storage.RemoteActionsRepository
import org.koitharu.toadlink.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class ActionsViewModel @Inject constructor(
    private val connectionManager: SshConnectionManager,
    private val repository: RemoteActionsRepository,
) : MviViewModel<ActionsState, ActionsIntent, ActionsEffect>(ActionsState()) {

    private val executionState = MutableStateFlow<PersistentMap<Int, ExecutionState>>(
        persistentMapOf()
    )

    init {
        viewModelScope.launch {
            connectionManager.activeConnection.flatMapLatest {
                if (it == null) {
                    emptyFlow()
                } else {
                    val deviceId = it.host.id
                    repository.observeAll(deviceId)
                }
            }.combine(executionState) { actions, execution ->
                actions.map { action ->
                    val state = execution[action.id]
                    ActionItem(
                        action = action,
                        state = state ?: ExecutionState.None,
                    )
                }
            }.collect {
                state.value = ActionsState(
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
            executionState.update { it.put(action.id, ExecutionState.Running) }
            runCatchingCancellable {
                connectionManager.awaitConnection()
                    .execute(action.cmdline)
            }.onSuccess { result ->
                executionState.update { it.put(action.id, ExecutionState.Success(result)) }
            }.onFailure { error ->
                executionState.update { it.put(action.id, ExecutionState.Failed(error)) }
            }
        }
    }
}