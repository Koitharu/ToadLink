package org.koitharu.toadlink.utils.coil

import coil3.Uri
import coil3.request.Options
import org.koitharu.toadlink.client.SshConnectionManager

class GstVideoThumbnailFetcher internal constructor(
    data: Uri, options: Options, connectionManager: SshConnectionManager,
) : ThumbnailFetcher(data, options, connectionManager) {

    override fun getCmdline(data: Uri, options: Options) = buildString {
        append("gst-video-thumbnailer -p \"")
        append(data.path)
        append("\" -s ")
        append(options.size.singleSize())
        append(" -o /dev/stdout")
    }

    override fun mimeType(data: Uri) = "image/png"
}