package org.koitharu.toadlink.utils.coil.thumbnails

import org.koitharu.toadlink.core.util.escape
import org.koitharu.toadlink.files.fs.MimeType

data class Thumbnailer(
    val tryExec: String?,
    val exec: String,
    val mimeTypes: List<MimeType>,
) {

    fun getCommandLine(
        inputPath: String,
        size: Int
    ) = exec.replace("%s", size.toString())
        .replace("%u", "file://${inputPath.escape()}")
        .replace("%i", inputPath.escape())
        .replace("%o", "/dev/stdout")

    fun isSupported(mimeType: MimeType) = mimeTypes.any { type ->
        type.type == mimeType.type &&
                (type.subtype == MimeType.ANY || type.subtype == mimeType.subtype)
    }
}