package org.koitharu.toadlink.files

import java.io.File

internal sealed interface FileManagerEffect {

    data class OnError(
        val error: Throwable,
    ) : FileManagerEffect

    data class OpenExternal(
        val file: File,
    ) : FileManagerEffect
}