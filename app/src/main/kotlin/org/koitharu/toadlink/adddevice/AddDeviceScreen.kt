package org.koitharu.toadlink.adddevice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import org.koitharu.toadlink.adddevice.AddDeviceIntent.UpdateHostname
import org.koitharu.toadlink.adddevice.AddDeviceIntent.UpdatePassword
import org.koitharu.toadlink.adddevice.AddDeviceIntent.UpdatePort
import org.koitharu.toadlink.adddevice.AddDeviceIntent.UpdateUsername
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.nav.LocalRouter
import org.koitharu.toadlink.ui.util.plus

@Composable
fun AddDeviceScreen(
    initialAddress: String?,
) {
    val viewModel = hiltViewModel<AddDeviceViewModel, AddDeviceViewModel.Factory> {
        it.create(initialAddress)
    }
    AddDeviceContent(
        state = viewModel.collectState().value,
        handleIntent = viewModel::handleIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDeviceContent(
    state: AddDeviceState,
    handleIntent: (AddDeviceIntent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_device),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomButtonsBar(handleIntent)
        },
        content = { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingValues(16.dp) + contentPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.hostname,
                    singleLine = true,
                    isError = state.hostnameError != 0,
                    onValueChange = { handleIntent(UpdateHostname(it)) },
                    label = { Text(stringResource(R.string.host_address)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    )
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.port.toString(),
                    singleLine = true,
                    isError = state.portError != 0,
                    onValueChange = { handleIntent(UpdatePort(it.toInt())) },
                    label = { Text(stringResource(R.string.port)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    )
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.username,
                    singleLine = true,
                    isError = state.usernameError != 0,
                    onValueChange = { handleIntent(UpdateUsername(it)) },
                    label = { Text(stringResource(R.string.username)) },
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
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
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
                    }
                )
            }
        }
    )
}

@Composable
private fun BottomButtonsBar(
    handleIntent: (AddDeviceIntent) -> Unit,
) = BottomAppBar {
    val router = LocalRouter.current
    OutlinedButton(
        modifier = Modifier.weight(1f),
        onClick = { router.back() },
    ) {
        Text(stringResource(android.R.string.cancel))
    }
    Spacer(
        modifier = Modifier.width(16.dp)
    )
    Button(
        modifier = Modifier.weight(1f),
        onClick = {
            handleIntent(AddDeviceIntent.SaveDevice)
        }
    ) {
        Text(stringResource(R.string.done))
    }
}

@Preview
@Composable
private fun AddDeviceContentPreview() = AddDeviceContent(
    state = AddDeviceState(null).copy(
        username = "test",
        usernameError = R.string.error_generic
    ),
    handleIntent = {}
)