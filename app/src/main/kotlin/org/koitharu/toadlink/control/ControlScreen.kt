package org.koitharu.toadlink.control

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.koitharu.toadlink.actions.ui.list.ActionsContent
import org.koitharu.toadlink.control.ControlIntent.SwitchSection
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.mpris.ui.PlayerControlContent
import org.koitharu.toadlink.ui.R

@Composable
fun ControlScreen(deviceId: Int) {
    val viewModel = hiltViewModel<ControlViewModel, ControlViewModel.Factory> {
        it.create(deviceId)
    }
    val state by viewModel.collectState()
    val snackbarHostState = remember { SnackbarHostState() }

    ControlContent(
        state = state,
        snackbarHostState = snackbarHostState,
        handleIntent = viewModel::handleIntent,
    )
}

@Composable
private fun ControlContent(
    state: ControlState,
    snackbarHostState: SnackbarHostState,
    handleIntent: (ControlIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.device?.displayName ?: stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                subtitle = {
                    state.device?.let { device ->
                        Text(
                            text = device.username,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = state is ControlState.Connected,
                enter = slideInVertically(),
                exit = slideOutVertically(),
            ) {
                check(state is ControlState.Connected)
                NavigationBar {
                    NavigationItem(
                        isSelected = state.section == ControlSection.ACTIONS,
                        icon = R.drawable.ic_click,
                        label = stringResource(R.string.actions),
                        onClick = { handleIntent(SwitchSection(ControlSection.ACTIONS)) }
                    )
                    NavigationItem(
                        isSelected = state.section == ControlSection.MPRIS,
                        icon = R.drawable.ic_media,
                        label = stringResource(R.string.media),
                        onClick = { handleIntent(SwitchSection(ControlSection.MPRIS)) }
                    )
                    NavigationItem(
                        isSelected = state.section == ControlSection.FILES,
                        icon = R.drawable.ic_folder,
                        label = stringResource(R.string.files),
                        onClick = { handleIntent(SwitchSection(ControlSection.FILES)) }
                    )
                }
            }
        }
    ) { contentPadding ->
        when (state) {
            is ControlState.Connected -> when (state.section) {
                ControlSection.ACTIONS -> ActionsContent(
                    contentPadding = contentPadding,
                    snackbarHostState = snackbarHostState,
                )
                ControlSection.MPRIS -> PlayerControlContent(
                    contentPadding = contentPadding,
                    snackbarHostState = snackbarHostState,
                )

                ControlSection.FILES -> Text("Not implemented")
            }

            is ControlState.Connecting -> ConnectingState(
                modifier = Modifier.padding(contentPadding)
            )

            is ControlState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = state.error.message ?: stringResource(R.string.error_generic)
                )
            }
        }
    }
}

@Composable
private fun ConnectingState(
    modifier: Modifier = Modifier,
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .then(modifier),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    LoadingIndicator()
    Text(
        modifier = Modifier.padding(
            top = 16.dp,
            bottom = 12.dp,
        ),
        text = stringResource(R.string.connecting_),
        style = MaterialTheme.typography.titleMedium,
    )
    Button(
        onClick = { /* TODO */ }
    ) {
        Text(text = stringResource(android.R.string.cancel))
    }
}

@Composable
private fun RowScope.NavigationItem(
    isSelected: Boolean,
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
) = NavigationBarItem(
    selected = isSelected,
    onClick = onClick,
    icon = { Icon(painterResource(icon), label) },
    label = { Text(label) },
)

@Preview
@Composable
private fun PreviewControlContent() = ControlContent(
    state = ControlState.Connected(
        section = ControlSection.MPRIS,
        device = DeviceDescriptor(
            id = 1,
            hostname = "19.168.0.4",
            port = 22,
            alias = null,
            username = "stub",
            password = "stub"
        )
    ),
    snackbarHostState = SnackbarHostState(),
    handleIntent = { /* no-op */ }
)

@Preview
@Composable
private fun ConnectingStatePreview() = ConnectingState()