package org.koitharu.toadlink.utils.coil

import coil3.Uri
import coil3.request.Options
import coil3.size.pxOrElse
import org.koitharu.toadlink.client.SshConnectionManager

class VideoThumbnailFetcher internal constructor(
    data: Uri, options: Options, connectionManager: SshConnectionManager,
) : ThumbnailFetcher(data, options, connectionManager) {

    override fun getCmdline(data: Uri, options: Options) = buildString {
        append("ffmpegthumbnailer -i \"")
        append(data.path)
        append("\" -o - -s ")
        append(options.size.width.pxOrElse { SIZE_MAX })
        append(" -c png")
    }

    override fun mimeType(data: Uri) = "image/png"
}