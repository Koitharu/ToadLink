package org.koitharu.toadlink.files

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.koitharu.toadlink.files.fs.SshFile
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviIntentHandler

@Composable
internal fun FileContextMenu(
    file: SshFile,
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    handleIntent: MviIntentHandler<FileManagerIntent>,
) = DropdownMenu(
    expanded = isExpanded,
    onDismissRequest = onDismissRequest,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.get_and_open)) },
        onClick = {
            handleIntent(FileManagerIntent.OpenFile(file))
            onDismissRequest()
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.get_and_save)) },
        onClick = {
            /* TODO */
            onDismissRequest()
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.get_and_share)) },
        onClick = {
            /* TODO */
            onDismissRequest()
        },
    )
    HorizontalDivider()
    DropdownMenuItem(
        text = { Text(stringResource(R.string.rename)) },
        onClick = {
            /* TODO */
            onDismissRequest()
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.delete)) },
        onClick = {
            /* TODO */
            onDismissRequest()
        },
    )
}
