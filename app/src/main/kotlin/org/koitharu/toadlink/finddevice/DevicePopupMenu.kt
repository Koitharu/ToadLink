package org.koitharu.toadlink.finddevice

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.editor.EditDeviceDestination
import org.koitharu.toadlink.finddevice.FindDeviceIntent.Disconnect
import org.koitharu.toadlink.finddevice.FindDeviceIntent.Remove
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter

@Composable
internal fun DevicePopupMenu(
    device: DeviceDescriptor,
    isConnected: Boolean,
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    handleIntent: MviIntentHandler<FindDeviceIntent>,
) {
    var isRemoveDialogVisible by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
    ) {
        val router = LocalRouter.current
        if (isConnected) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.remove)) },
                onClick = {
                    onDismissRequest()
                    handleIntent(Disconnect(device.id))
                },
            )
            HorizontalDivider()
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.edit)) },
            onClick = {
                onDismissRequest()
                router.add(EditDeviceDestination(device.id))
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.remove)) },
            onClick = {
                onDismissRequest()
                isRemoveDialogVisible = true
            },
        )
    }
    if (isRemoveDialogVisible) {
        RemoveAlertDialog(
            device = device,
            onDismissRequest = { isRemoveDialogVisible = false },
            handleIntent = handleIntent,
        )
    }
}

@Composable
private fun RemoveAlertDialog(
    device: DeviceDescriptor,
    onDismissRequest: () -> Unit,
    handleIntent: MviIntentHandler<FindDeviceIntent>,
) = AlertDialog(
    title = {
        Text(stringResource(R.string.remove_device))
    },
    text = {
        Text(stringResource(R.string.remove_device_confirmation, device.displayName))
    },
    confirmButton = {
        TextButton(
            onClick = { handleIntent(Remove(device.id)) }
        ) {
            Text(stringResource(R.string.remove))
        }
    },
    dismissButton = {
        TextButton(
            onClick = onDismissRequest
        ) {
            Text(stringResource(android.R.string.cancel))
        }
    },
    onDismissRequest = onDismissRequest
)