package org.koitharu.toadlink.actions.ui.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.toadlink.actions.ui.ExecutionState
import org.koitharu.toadlink.actions.ui.editor.ActionEditorDestination
import org.koitharu.toadlink.actions.ui.list.ActionsIntent.Execute
import org.koitharu.toadlink.client.RemoteProcessException
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.composables.EmptyState
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter
import org.koitharu.toadlink.ui.util.displayMessage
import org.koitharu.toadlink.ui.util.getDisplayMessage
import org.koitharu.toadlink.ui.util.themeAttributeSize

@Composable
fun ActionsContent(
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = hiltViewModel<ActionsViewModel>()
    val state by viewModel.collectState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is ActionsEffect.OnError -> snackbarHostState.showSnackbar(
                    effect.error.getDisplayMessage(context)
                )
            }
        }.launchIn(this)
    }
    ActionsList(
        contentPadding = contentPadding,
        state = state,
        handleIntent = viewModel,
    )
}

@Composable
private fun ActionsList(
    contentPadding: PaddingValues,
    state: ActionsState,
    handleIntent: MviIntentHandler<ActionsIntent>,
) = when {
    state.isLoading -> Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }

    state.actions.isEmpty() -> EmptyState(
        modifier = Modifier.padding(contentPadding)
    )

    else -> LazyColumn(
        contentPadding = contentPadding,
    ) {
        items(
            items = state.actions,
            key = { it.action.id },
            contentType = { RemoteAction::class },
        ) {
            ActionRow(
                item = it,
                handleIntent = handleIntent,
            )
        }
        item {
            AddButton()
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier,
) = EmptyState(
    modifier = Modifier
        .fillMaxSize()
        .then(modifier),
    icon = painterResource(R.drawable.ic_click),
    message = stringResource(R.string.no_actions_added),
) {
    val router = LocalRouter.current
    Button(
        onClick = { router.navigate(ActionEditorDestination(null)) }
    ) {
        Text(text = stringResource(R.string.add_action))
    }
}

@Composable
private fun ActionRow(
    item: ActionItem,
    handleIntent: MviIntentHandler<ActionsIntent>,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
        onClick = { isMenuExpanded = true }
    ) {
        Row(
            modifier = Modifier
                .padding(
                    vertical = 8.dp,
                    horizontal = 16.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Icon(
                    painter = painterResource(R.drawable.ic_cmd),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
            ) {
                Text(
                    text = item.action.name,
                    style = MaterialTheme.typography.titleMedium
                )
                when (item.state) {
                    is ExecutionState.Failed -> Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = item.state.error.displayMessage(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    is ExecutionState.Success -> Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = item.state.result,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorResource(R.color.green),
                    )

                    else -> Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = item.action.cmdline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = { handleIntent(Execute(item.action)) },
                enabled = item.state != ExecutionState.Running,
            ) {
                AnimatedContent(item.state == ExecutionState.Running) { isRunning ->
                    if (isRunning) {
                        LoadingIndicator()
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_play),
                            contentDescription = stringResource(R.string.execute),
                        )
                    }
                }
            }
            Box {
                IconButton(
                    onClick = { isMenuExpanded = true },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = stringResource(R.string.menu),
                    )
                }
                ActionMenu(
                    actionItem = item,
                    isExpanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    handleIntent = handleIntent,
                )
            }
        }
    }
}

@Composable
private fun ActionMenu(
    actionItem: ActionItem,
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    handleIntent: MviIntentHandler<ActionsIntent>,
) = DropdownMenu(
    expanded = isExpanded,
    onDismissRequest = onDismissRequest,
) {
    val router = LocalRouter.current
    DropdownMenuItem(
        text = { Text(stringResource(R.string.execute)) },
        onClick = { handleIntent(Execute(actionItem.action)) },
        enabled = actionItem.state != ExecutionState.Running,
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.edit_action)) },
        onClick = { router.add(ActionEditorDestination(actionItem.action)) },
    )
}

@Composable
private fun AddButton() {
    val router = LocalRouter.current
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
        onClick = {
            router.navigate(ActionEditorDestination(null))
        }
    ) {
        Row(
            modifier = Modifier.padding(
                vertical = 8.dp,
                horizontal = 16.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = stringResource(R.string.add_action),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEmptyList() = MaterialTheme {
    ActionsList(
        contentPadding = PaddingValues.Zero,
        state = ActionsState(
            actions = persistentListOf(),
            isLoading = false
        ),
        handleIntent = { /* no-op */ }
    )
}

@Preview
@Composable
private fun PreviewActionsList() = MaterialTheme {
    ActionsList(
        contentPadding = PaddingValues.Zero,
        state = ActionsState(
            actions = persistentListOf(
                ActionItem(RemoteAction(1, "Shutdown", "stub"), ExecutionState.None),
                ActionItem(RemoteAction(2, "Reboot", "stub"), ExecutionState.Running),
                ActionItem(
                    action = RemoteAction(3, "Failed action", "stub"),
                    state = ExecutionState.Failed(
                        RemoteProcessException(
                            1,
                            "No such file or directory"
                        )
                    )
                ),
                ActionItem(
                    action = RemoteAction(4, "Success action", "stub"),
                    state = ExecutionState.Success(LoremIpsum(12).values.joinToString(" ")),
                ),
            ),
            isLoading = false
        ),
        handleIntent = { /* no-op */ }
    )
}