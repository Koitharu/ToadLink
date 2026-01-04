package org.koitharu.toadlink.utils.coil

import coil3.Uri
import coil3.request.Options
import coil3.size.pxOrElse
import coil3.util.MimeTypeMap
import org.koitharu.toadlink.client.SshConnectionManager

class PdfThumbnailFetcher internal constructor(
    data: Uri, options: Options, connectionManager: SshConnectionManager,
) : ThumbnailFetcher(data, options, connectionManager) {

    override fun getCmdline(data: Uri, options: Options) = buildString {
        append("convert \"")
        append(data.path)
        append("\"[0] -thumbnail ")
        append(options.size.width.pxOrElse { SIZE_MAX })
        append('x')
        append(options.size.height.pxOrElse { SIZE_MAX })
        append(" -background white -alpha remove PNG:-")
    }

    override fun mimeType(data: Uri): String? {
        return MimeTypeMap.getMimeTypeFromUrl(data.toString())
    }
}