package org.koitharu.toadlink.files

import okio.Path
import org.koitharu.toadlink.files.fs.SshFile

internal sealed interface FileManagerIntent {

    data class Navigate(
        val path: Path,
    ) : FileManagerIntent

    data object NavigateUp : FileManagerIntent

    data class OpenFile(
        val file: SshFile,
    ) : FileManagerIntent

    data object CancelFileTransfer : FileManagerIntent
}