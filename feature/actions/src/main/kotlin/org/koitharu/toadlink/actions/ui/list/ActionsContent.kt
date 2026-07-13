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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.toadlink.actions.ui.ExecutionState
import org.koitharu.toadlink.actions.ui.editor.ActionEditorDestination
import org.koitharu.toadlink.actions.ui.list.ActionsIntent.Execute
import org.koitharu.toadlink.actions.ui.list.preview.ActionListPreviewProvider
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.composables.EmptyState
import org.koitharu.toadlink.ui.composables.IconButtonWithTooltip
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter
import org.koitharu.toadlink.ui.util.displayMessage
import org.koitharu.toadlink.ui.util.getDisplayMessage
import org.koitharu.toadlink.ui.util.themeAttributeSize

@Composable
fun ActionsContent(
    device: DeviceDescriptor,
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = hiltViewModel<ActionsViewModel, ActionsViewModel.Factory> {
        it.create(device)
    }
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
        host = state.host,
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
            AddButton(state.host)
        }
    }
}

@Composable
private fun EmptyState(
    host: DeviceDescriptor,
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
        onClick = { router.navigate(ActionEditorDestination(host, null)) }
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
    var isConfirmationVisible by remember { mutableStateOf(false) }
    if (isConfirmationVisible) {
        RunConfirmationDialog(
            action = item.action,
            onDismissRequest = { isConfirmationVisible = false },
            onConfirm = { handleIntent(Execute(item.action)) }
        )
    }
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
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

                    is ExecutionState.Success -> if (item.state.result.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(top = 2.dp),
                            text = stringResource(R.string.executed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.surfaceDim,
                        )
                    } else {
                        Text(
                            modifier = Modifier.padding(top = 2.dp),
                            text = item.state.result,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorResource(R.color.green),
                        )
                    }

                    else -> Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = item.action.cmdline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButtonWithTooltip(
                onClick = {
                    if (item.action.isConfirmationRequired) {
                        isConfirmationVisible = true
                    } else {
                        handleIntent(Execute(item.action))
                    }
                },
                tooltip = stringResource(R.string.execute),
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
                IconButtonWithTooltip(
                    onClick = { isMenuExpanded = true },
                    tooltip = stringResource(R.string.menu)
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
        text = { Text(stringResource(R.string.edit_action)) },
        onClick = { router.add(ActionEditorDestination(actionItem.host, actionItem.action)) },
        enabled = actionItem.state != ExecutionState.Running,
    )
}

@Composable
private fun AddButton(
    host: DeviceDescriptor,
) {
    val router = LocalRouter.current
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
        onClick = {
            router.navigate(ActionEditorDestination(host, null))
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
            host = DeviceDescriptor(
                id = 1,
                hostname = "192.168.8.77",
                port = 22,
                alias = null,
                username = "user",
                password = "passw",
                key = null,
                lastConnect = null,
                connectAutomatically = false,
            ),
            actions = persistentListOf(),
            isLoading = false
        ),
        handleIntent = { /* no-op */ }
    )
}

@Preview
@Composable
private fun PreviewActionsList(
    @PreviewParameter(ActionListPreviewProvider::class) actions: PersistentList<ActionItem>,
) = MaterialTheme {
    ActionsList(
        contentPadding = PaddingValues.Zero,
        state = ActionsState(
            host = DeviceDescriptor(
                id = 1,
                hostname = "192.168.8.77",
                port = 22,
                alias = null,
                username = "user",
                password = "passw",
                key = null,
                lastConnect = null,
                connectAutomatically = false,
            ),
            actions = actions,
            isLoading = false
        ),
        handleIntent = { /* no-op */ }
    )
}