package org.koitharu.toadlink.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.koitharu.toadlink.core.util.toIntLenient
import org.koitharu.toadlink.editor.DeviceEditorEffect.Back
import org.koitharu.toadlink.editor.DeviceEditorEffect.OnError
import org.koitharu.toadlink.editor.DeviceEditorEffect.OpenDevice
import org.koitharu.toadlink.editor.DeviceEditorIntent.ToggleConnectNow
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdateAlias
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdateHostname
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdatePassword
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdatePort
import org.koitharu.toadlink.editor.DeviceEditorIntent.UpdateUsername
import org.koitharu.toadlink.nav.ControlDestination
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.composables.BackNavigationIcon
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter
import org.koitharu.toadlink.ui.util.getDisplayMessage
import org.koitharu.toadlink.ui.util.plus

@Composable
fun AddDeviceScreen(
    deviceId: Int,
    initialAddress: String?,
) {
    val viewModel = hiltViewModel<DeviceEditorViewModel, DeviceEditorViewModel.Factory> {
        it.create(deviceId, initialAddress)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val router = LocalRouter.current
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnError -> snackbarHostState.showSnackbar(
                    effect.error.getDisplayMessage(context)
                )

                is OpenDevice -> router.changeRoot(
                    ControlDestination(effect.deviceId)
                )

                Back -> router.removeLastOrNull()
            }
        }
    }
    AddDeviceContent(
        state = viewModel.collectState().value,
        snackbarHostState = snackbarHostState,
        handleIntent = viewModel,
    )
}

@Composable
private fun AddDeviceContent(
    state: DeviceEditorState,
    snackbarHostState: SnackbarHostState,
    handleIntent: MviIntentHandler<DeviceEditorIntent>
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.isNewDevice) {
                                R.string.add_device
                            } else {
                                R.string.edit_device
                            }
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    BackNavigationIcon()
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomButtonsBar(
                isDoneEnabled = state.isDoneEnabled(),
                handleIntent = handleIntent,
            )
        },
        content = { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingValues(16.dp) + contentPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState()),
            ) {

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.hostname,
                    singleLine = true,
                    isError = state.hostnameError != 0,
                    onValueChange = { handleIntent(UpdateHostname(it)) },
                    enabled = !state.isLoading,
                    label = { Text(stringResource(R.string.host_address)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    ),
                    supportingText = {
                        AnimatedVisibility(state.hostnameError != 0) {
                            Text(stringResource(state.hostnameError))
                        }
                    },
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.port.let {
                        if (it == 0) "" else it.toString()
                    },
                    singleLine = true,
                    isError = state.portError != 0,
                    onValueChange = { handleIntent(UpdatePort(it.toIntLenient())) },
                    label = { Text(stringResource(R.string.port)) },
                    enabled = !state.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    ),
                    supportingText = {
                        AnimatedVisibility(state.portError != 0) {
                            Text(stringResource(state.portError))
                        }
                    },
                )

                Text(
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    text = stringResource(R.string.authentication),
                    style = MaterialTheme.typography.titleMedium,
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.username,
                    singleLine = true,
                    isError = state.usernameError != 0,
                    onValueChange = { handleIntent(UpdateUsername(it)) },
                    label = { Text(stringResource(R.string.username)) },
                    enabled = !state.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    ),
                    supportingText = {
                        AnimatedVisibility(state.usernameError != 0) {
                            Text(stringResource(state.usernameError))
                        }
                    },
                )

                var isPasswordVisible by remember { mutableStateOf(false) }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.password,
                    onValueChange = { handleIntent(UpdatePassword(it)) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.password)) },
                    enabled = !state.isLoading,
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { isPasswordVisible = !isPasswordVisible }
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isPasswordVisible) {
                                        R.drawable.ic_visible
                                    } else {
                                        R.drawable.ic_hidden
                                    }
                                ),
                                contentDescription = stringResource(
                                    if (isPasswordVisible) {
                                        R.string.hide_password
                                    } else {
                                        R.string.show_password
                                    }
                                )
                            )
                        }
                    },
                )

                Text(
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    text = stringResource(R.string.other),
                    style = MaterialTheme.typography.titleMedium,
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.alias,
                    singleLine = true,
                    onValueChange = { handleIntent(UpdateAlias(it)) },
                    enabled = !state.isLoading,
                    label = { Text(stringResource(R.string.device_name_opt)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                )

                if (state.isNewDevice) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .clip(MaterialTheme.shapes.small)
                            .padding(top = 4.dp)
                            .clickable(
                                onClick = { handleIntent(ToggleConnectNow) }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f),
                            text = stringResource(R.string.connect_now),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Switch(
                            checked = state.connectNow,
                            onCheckedChange = null,
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun BottomButtonsBar(
    isDoneEnabled: Boolean,
    handleIntent: MviIntentHandler<DeviceEditorIntent>,
) = BottomAppBar {
    val router = LocalRouter.current
    OutlinedButton(
        modifier = Modifier.weight(1f),
        onClick = { router.removeLastOrNull() },
    ) {
        Text(stringResource(android.R.string.cancel))
    }
    Spacer(
        modifier = Modifier.width(16.dp)
    )
    Button(
        modifier = Modifier.weight(1f),
        enabled = isDoneEnabled,
        onClick = {
            handleIntent(DeviceEditorIntent.SaveDevice)
        }
    ) {
        Text(stringResource(R.string.done))
    }
}

@Preview
@Composable
private fun AddDeviceContentPreview() = AddDeviceContent(
    state = DeviceEditorState(null, isNewDevice = true).copy(
        username = "test",
        usernameError = R.string.error_generic
    ),
    snackbarHostState = SnackbarHostState(),
    handleIntent = MviIntentHandler.NoOp,
)