package org.koitharu.toadlink.files

import org.koitharu.toadlink.files.fs.MimeType
import java.io.File

internal sealed interface FileManagerEffect {

    data class OnError(
        val error: Throwable,
    ) : FileManagerEffect

    data class OpenExternal(
        val file: File,
    ) : FileManagerEffect

    data class OpenShare(
        val file: File,
        val mimeType: MimeType,
    ) : FileManagerEffect

    data class OnFileSaved(
        val fileName: String,
    ): FileManagerEffect
}