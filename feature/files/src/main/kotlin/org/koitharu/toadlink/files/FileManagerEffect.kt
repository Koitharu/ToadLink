package org.koitharu.toadlink.files

import android.net.Uri

internal sealed interface FileManagerEffect {

    data class OnError(
        val error: Throwable,
    ) : FileManagerEffect

    data class OpenExternal(
        val uri: Uri,
    ) : FileManagerEffect
}