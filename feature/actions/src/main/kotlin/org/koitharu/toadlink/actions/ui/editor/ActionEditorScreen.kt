package org.koitharu.toadlink.actions.ui.editor

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.actions.ui.editor.ActionEditorIntent.ApplyCompletion
import org.koitharu.toadlink.actions.ui.editor.ActionEditorIntent.OnCmdlineChanged
import org.koitharu.toadlink.actions.ui.editor.ActionEditorIntent.Save
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter
import org.koitharu.toadlink.ui.theme.ToadLinkTheme

@Composable
fun ActionEditorScreen(
    action: RemoteAction?,
) {
    val viewModel = hiltViewModel<ActionEditorViewModel, ActionEditorViewModel.Factory> {
        it.create(action)
    }
    val state by viewModel.collectState()
    ActionEditorContent(
        state = state,
        handleIntent = viewModel,
    )
}

@Composable
private fun ActionEditorContent(
    state: ActionEditorState,
    handleIntent: MviIntentHandler<ActionEditorIntent>,
) = Scaffold(
    modifier = Modifier.imePadding(),
    topBar = {
        TopAppBar(
            title = {
                Text(
                    stringResource(
                        if (state.actionId == -1) {
                            R.string.add_action
                        } else {
                            R.string.edit_action
                        }
                    )
                )
            },
            navigationIcon = {
                val router = LocalRouter.current
                IconButton(
                    onClick = { router.back() },
                    content = {
                        Icon(
                            painterResource(R.drawable.ic_back),
                            stringResource(R.string.close)
                        )
                    },
                )
            }
        )
    },
    content = { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(padding)
                .scrollable(rememberScrollState(0), Orientation.Vertical)
        ) {
            val fieldModifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 6.dp,
                    horizontal = 12.dp,
                )
            val keyboardController = LocalSoftwareKeyboardController.current
            if (state.isLoading) {
                keyboardController?.hide()
            }
            TextField(
                modifier = fieldModifier,
                value = state.name,
                enabled = !state.isLoading,
                onValueChange = { handleIntent(ActionEditorIntent.OnNameChanged(it)) },
                label = { Text(stringResource(R.string.name)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    autoCorrectEnabled = true,
                    imeAction = ImeAction.Next,
                ),
                singleLine = true,
            )
            CommandEdit(
                modifier = fieldModifier,
                state = state,
                keyboardController = keyboardController,
                handleIntent = handleIntent,
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp)
            )
            Button(
                onClick = {
                    handleIntent(Save)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                enabled = !state.isLoading && state.isSaveEnabled,
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
)

@Composable
private fun CommandEdit(
    modifier: Modifier,
    state: ActionEditorState,
    keyboardController: SoftwareKeyboardController?,
    handleIntent: MviIntentHandler<ActionEditorIntent>,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isExpanded = focusState.isFocused
                },
            value = state.cmdline,
            enabled = !state.isLoading,
            onValueChange = { handleIntent(OnCmdlineChanged(it)) },
            label = { Text(stringResource(R.string.command)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Text,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
            },
            singleLine = true,
        )
        DropdownMenu(
            modifier = Modifier
                .heightIn(max = 320.dp)
                .scrollable(rememberScrollState(), Orientation.Vertical),
            expanded = isExpanded && state.cmdlineCompletion.isNotEmpty(),
            onDismissRequest = { },
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
        ) {
            state.cmdlineCompletion.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(text = item)
                    },
                    onClick = {
                        handleIntent(ApplyCompletion(item))
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewActionEditor() = ToadLinkTheme {
    ActionEditorContent(
        state = ActionEditorState(
            actionId = 1,
            name = "Reboot",
            cmdline = TextFieldValue("systemctl reboot"),
            cmdlineCompletion = persistentListOf(),
            isLoading = false,
        ),
        handleIntent = MviIntentHandler.NO_OP
    )
}