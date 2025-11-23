package org.koitharu.toadlink.actions.ui.list

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.toadlink.actions.ui.editor.ActionEditorDestination
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.nav.LocalRouter
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
        handleIntent = viewModel::handleIntent,
    )
}

@Composable
private fun ActionsList(
    contentPadding: PaddingValues,
    state: ActionsState,
    handleIntent: (ActionsIntent) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = state.actions,
            key = { it.id },
            contentType = { RemoteAction::class },
        ) {
            ActionItem(
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
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .then(modifier),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Icon(
        painter = painterResource(R.drawable.ic_click),
        contentDescription = stringResource(R.string.actions),
    )
    Text(
        modifier = Modifier.padding(
            top = 16.dp,
            bottom = 12.dp,
        ),
        text = stringResource(R.string.no_actions_added),
        style = MaterialTheme.typography.titleMedium,
    )
    val router = LocalRouter.current
    Button(
        onClick = { router.navigate(ActionEditorDestination(null)) }
    ) {
        Text(text = stringResource(R.string.add_action))
    }
}

@Composable
private fun ActionItem(
    item: RemoteAction,
    handleIntent: (ActionsIntent) -> Unit,
) {
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
        onClick = {

        }
    ) {
        Row(
            modifier = Modifier.padding(
                vertical = 8.dp,
                horizontal = 16.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Icon(
                    painter = painterResource(R.drawable.ic_pc_phone),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = item.cmdline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
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
                text = "Add device manually",
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
                RemoteAction(1, "Shutdown", "stub"),
                RemoteAction(2, "Reboot", "stub"),
            ),
            isLoading = false
        ),
        handleIntent = { /* no-op */ }
    )
}