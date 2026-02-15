package org.koitharu.toadlink.utils.coil

import coil3.Uri
import coil3.request.Options
import org.koitharu.toadlink.client.SshConnectionManager

class FfmpegVideoThumbnailFetcher internal constructor(
    data: Uri, options: Options, connectionManager: SshConnectionManager,
) : ThumbnailFetcher(data, options, connectionManager) {

    override fun getCmdline(data: Uri, options: Options) = buildString {
        append("ffmpegthumbnailer -i \"")
        append(data.path)
        append("\" -o /dev/stdout -s ")
        append(options.size.singleSize())
        append(" -c png")
    }

    override fun mimeType(data: Uri) = "image/png"
}