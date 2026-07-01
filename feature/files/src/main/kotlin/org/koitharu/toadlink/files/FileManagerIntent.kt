package org.koitharu.toadlink.files

import android.net.Uri
import okio.Path
import org.koitharu.toadlink.files.fs.SshFile

internal sealed interface FileManagerIntent {

    sealed interface TransferFileIntent : FileManagerIntent {
        val file: SshFile
    }

    data class Navigate(
        val path: Path,
    ) : FileManagerIntent

    data object NavigateUp : FileManagerIntent

    data class OpenFile(
        override val file: SshFile,
    ) : TransferFileIntent

    data class ShareFile(
        override val file: SshFile,
    ) : TransferFileIntent

    data class SaveFile(
        override val file: SshFile,
        val target: Uri,
    ) : TransferFileIntent

    data object CancelFileTransfer : FileManagerIntent

    data class DeleteFile(
        val file: SshFile,
    ) : FileManagerIntent

    data class RenameFile(
        val file: SshFile,
        val newName: String,
    ) : FileManagerIntent
}