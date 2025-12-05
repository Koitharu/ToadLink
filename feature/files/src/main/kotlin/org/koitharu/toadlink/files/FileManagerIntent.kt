package org.koitharu.toadlink.files

import org.koitharu.toadlink.core.fs.SshFile
import org.koitharu.toadlink.core.fs.SshPath

internal sealed interface FileManagerIntent {

    data class Navigate(
        val path: SshPath,
    ) : FileManagerIntent

    data object NavigateUp : FileManagerIntent

    data class OpenFile(
        val file: SshFile,
    ) : FileManagerIntent

    data object CancelFileTransfer : FileManagerIntent
}