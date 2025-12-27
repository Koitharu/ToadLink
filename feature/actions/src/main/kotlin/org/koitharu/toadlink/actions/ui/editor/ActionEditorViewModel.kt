package org.koitharu.toadlink.actions.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koitharu.toadlink.actions.ui.editor.ActionEditorEffect.Close
import org.koitharu.toadlink.actions.ui.editor.ActionEditorEffect.OnError
import org.koitharu.toadlink.actions.ui.editor.ActionEditorIntent.ApplyCompletion
import org.koitharu.toadlink.actions.ui.editor.ActionEditorIntent.OnCmdlineChanged
import org.koitharu.toadlink.actions.ui.editor.ActionEditorIntent.OnNameChanged
import org.koitharu.toadlink.actions.ui.editor.ActionEditorIntent.Save
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.client.getCmdCompletion
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.storage.RemoteActionsRepository
import org.koitharu.toadlink.ui.mvi.MviViewModel

@HiltViewModel(assistedFactory = ActionEditorViewModel.Factory::class)
internal class ActionEditorViewModel @AssistedInject constructor(
    @Assisted action: RemoteAction?,
    private val repository: RemoteActionsRepository,
    private val connectionManager: SshConnectionManager,
) : MviViewModel<ActionEditorState, ActionEditorIntent, ActionEditorEffect>(ActionEditorState(action)) {

    init {
        viewModelScope.launch(Dispatchers.Default) {
            state
                .map { it.cmdline.text.getWordAt(it.cmdline.selection.end) }
                .distinctUntilChanged()
                .debounce(300L)
                .mapLatest {
                    getCompletion(it)
                }.collect { completion ->
                    state.update { it.copy(cmdlineCompletion = completion) }
                }
        }
    }

    override fun handleIntent(intent: ActionEditorIntent) = when (intent) {
        is ApplyCompletion -> applyCompletion(intent.value)
        is OnCmdlineChanged -> state.update {
            it.copy(cmdline = intent.value)
        }

        is OnNameChanged -> state.update {
            it.copy(name = intent.value)
        }

        Save -> save()
    }

    fun applyCompletion(text: String) {
        val cmd = state.value.cmdline
        val position = cmd.selection.end
        val wordStart = cmd.text.lastIndexOf(' ', maxOf(position - 1, 0))
        val wordEnd = cmd.text.indexOf(' ', position)
        val prefix = if (wordStart < 0) "" else cmd.text.substring(0, wordStart)
        val suffix = if (wordEnd < 0) "" else cmd.text.substring(wordEnd + 1)
        val newValue = "$prefix $text $suffix".trimStart()
        state.value = state.value.copy(
            cmdline = TextFieldValue(
                text = newValue,
                selection = TextRange(newValue.length - suffix.length),
            )
        )
    }

    fun save() {
        viewModelScope.launch(Dispatchers.Default) {
            state.update { it.copy(isLoading = true) }
            val device = connectionManager.awaitConnection().host
            runCatchingCancellable {
                val action = RemoteAction(
                    id = state.value.actionId,
                    name = state.value.name,
                    cmdline = state.value.cmdline.text,
                )
                repository.store(action, deviceId = device.id)
            }.onFailure { error ->
                sendEffect(OnError(error))
            }.onSuccess {
                sendEffect(Close)
            }
            state.update { it.copy(isLoading = false) }
        }
    }

    private fun String.getWordAt(position: Int): String {
        if (position <= 0) {
            return ""
        }
        val index = lastIndexOf(' ', position - 1).coerceAtLeast(0)
        return substring(index, position)
    }

    private suspend fun getCompletion(cmdline: String) = if (cmdline.length > 3) {
        runCatchingCancellable {
            connectionManager.activeConnection.value?.getCmdCompletion(cmdline)
        }.getOrNull() ?: persistentListOf()
    } else {
        persistentListOf()
    }

    @AssistedFactory
    interface Factory {

        fun create(action: RemoteAction?): ActionEditorViewModel
    }
}