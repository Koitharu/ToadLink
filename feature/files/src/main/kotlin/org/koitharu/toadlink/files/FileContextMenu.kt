package org.koitharu.toadlink.files

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import org.koitharu.toadlink.files.fs.SshFile
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

}
