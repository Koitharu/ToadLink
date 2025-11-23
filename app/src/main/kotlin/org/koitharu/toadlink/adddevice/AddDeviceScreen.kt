package org.koitharu.toadlink.adddevice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.koitharu.toadlink.adddevice.AddDeviceIntent.UpdateHostname
import org.koitharu.toadlink.adddevice.AddDeviceIntent.UpdatePassword
import org.koitharu.toadlink.adddevice.AddDeviceIntent.UpdatePort
import org.koitharu.toadlink.adddevice.AddDeviceIntent.UpdateUsername
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
                    Text("Add device", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                    onValueChange = { handleIntent(UpdateHostname(it)) },
                    label = { Text("Host address") }
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.port.toString(),
                    singleLine = true,
                    onValueChange = { handleIntent(UpdatePort(it.toInt())) },
                    label = { Text("Port") }
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.username,
                    singleLine = true,
                    onValueChange = { handleIntent(UpdateUsername(it)) },
                    label = { Text("Username") }
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.password,
                    onValueChange = { handleIntent(UpdatePassword(it)) },
                    label = { Text("Password") }
                )
            }
        }
    )
}

@Composable
private fun BottomButtonsBar(
    handleIntent: (AddDeviceIntent) -> Unit,
) = BottomAppBar {
    OutlinedButton(
        modifier = Modifier.weight(1f),
        onClick = {},
    ) {
        Text("Test connection")
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
        Text("Done")
    }
}

@Preview
@Composable
private fun AddDeviceContentPreview() = AddDeviceContent(
    state = AddDeviceState(null),
    handleIntent = {}
)