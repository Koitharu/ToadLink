package org.koitharu.toadlink.finddevice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.NetworkDevice
import org.koitharu.toadlink.editor.AddDeviceDestination
import org.koitharu.toadlink.finddevice.FindDeviceEffect.OnError
import org.koitharu.toadlink.nav.ControlDestination
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.composables.DotIndicator
import org.koitharu.toadlink.ui.composables.ListHeader
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter
import org.koitharu.toadlink.ui.util.getDisplayMessage
import org.koitharu.toadlink.ui.util.themeAttributeSize

@Composable
fun FindDeviceScreen() {
    val viewModel = hiltViewModel<FindDeviceViewModel>()
    val state by viewModel.collectState()
    val snackbarHostState = remember { SnackbarHostState() }
    val router = LocalRouter.current
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnError -> snackbarHostState.showSnackbar(effect.error.getDisplayMessage(context))
                is FindDeviceEffect.OpenDevice -> router.changeRoot(
                    ControlDestination(effect.deviceId)
                )
            }
        }
    }
    FindDeviceContent(
        state = state,
        snackbarHostState = snackbarHostState,
        handleIntent = viewModel,
    )
}

@Composable
private fun FindDeviceContent(
    state: FindDeviceState,
    snackbarHostState: SnackbarHostState,
    handleIntent: MviIntentHandler<FindDeviceIntent>,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    FindDeviceMenu(handleIntent)
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { contentPadding ->
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = false,
                onRefresh = {
                    handleIntent(FindDeviceIntent.Refresh)
                }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = contentPadding
                ) {
                    if (state.savedDevices.isNotEmpty()) {
                        item {
                            ListHeader(text = stringResource(R.string.saved_devices_))
                        }
                        items(
                            items = state.savedDevices,
                            key = { it.id },
                            contentType = { DeviceDescriptor::class }
                        ) {
                            DeviceItem(
                                device = it,
                                isConnected = it.id == state.connectedDevice,
                                handleIntent = handleIntent
                            )
                        }
                    }
                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.available_devices_),
                            trailing = {
                                AnimatedVisibility(state.isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                    }
                    items(
                        items = state.availableDevices,
                        key = { it.address },
                    ) {
                        DeviceItem(it)
                    }
                    item {
                        AddDeviceButton()
                    }
                }
            }
        }
    )
}

@Composable
private fun AddDeviceButton() {
    val router = LocalRouter.current
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
        onClick = {
            router.navigate(AddDeviceDestination(null))
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
                text = stringResource(R.string.add_device_manually),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun DeviceItem(
    device: NetworkDevice,
) {
    val router = LocalRouter.current
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
        onClick = {
            router.navigate(AddDeviceDestination(device.address))
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
                painter = painterResource(R.drawable.ic_pc),
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.titleMedium
                )
                device.description?.let {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: DeviceDescriptor,
    isConnected: Boolean,
    handleIntent: MviIntentHandler<FindDeviceIntent>,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val router = LocalRouter.current
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
        onClick = {
            router.changeRoot(
                ControlDestination(device.id)
            )
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
                if (isConnected) {
                    DotIndicator(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd),
                        color = colorResource(R.color.toad)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                DevicePopupMenu(
                    device = device,
                    isConnected = isConnected,
                    isExpanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    handleIntent = handleIntent,
                )
            }
        }
    }
}

@Preview
@Composable
private fun FindDevicePreview() {
    FindDeviceContent(
        state = FindDeviceState(
            savedDevices = persistentListOf(
                DeviceDescriptor(
                    id = 1,
                    hostname = "192.168.0.17",
                    port = 22,
                    alias = "Laptop",
                    username = "",
                    password = ""
                ),
                DeviceDescriptor(
                    id = 2,
                    hostname = "192.168.0.5",
                    port = 22,
                    alias = null,
                    username = "",
                    password = ""
                )
            ),
            availableDevices = persistentListOf(
                NetworkDevice("192.168.0.23", null),
                NetworkDevice("192.168.0.117", "PC1"),
            ),
            isScanning = true,
            connectedDevice = 2,
        ),
        snackbarHostState = SnackbarHostState(),
        handleIntent = {}
    )
}