package org.koitharu.toadlink.utils.coil

import androidx.annotation.Px
import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Size
import coil3.size.pxOrElse
import coil3.util.MimeTypeMap
import okio.buffer
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.files.fs.MimeType.Companion.toMimeTypeOrNull

sealed class ThumbnailFetcher(
    private val data: Uri,
    private val options: Options,
    private val connectionManager: SshConnectionManager,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val command = getCmdline(data, options)
        val connection = connectionManager.awaitConnection()
        val source = connection.executeAsSource(command)
        return SourceFetchResult(
            source = ImageSource(
                source = source.buffer(),
                fileSystem = connection.fileSystem,
            ),
            mimeType = mimeType(data),
            dataSource = DataSource.NETWORK,
        )
    }

    protected abstract fun mimeType(data: Uri): String?

    protected abstract fun getCmdline(data: Uri, options: Options): String

    class Factory(
        private val connectionManager: SshConnectionManager,
    ) : Fetcher.Factory<Uri> {

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? {
            if (!data.isSsh() || !options.size.isThumbnail()) {
                return null
            }
            val mimeType = MimeTypeMap.getMimeTypeFromUrl(data.toString())?.toMimeTypeOrNull()
                ?: return null
            return when {
                mimeType.isImage -> ImageThumbnailFetcher(
                    data = data,
                    options = options,
                    connectionManager = connectionManager,
                )

                mimeType.isVideo -> CompositeFetcher(
                    GstVideoThumbnailFetcher(
                        data = data,
                        options = options,
                        connectionManager = connectionManager,
                    ),
                    FfmpegVideoThumbnailFetcher(
                        data = data,
                        options = options,
                        connectionManager = connectionManager,
                    )
                )

                mimeType.subtype == "pdf" -> PdfThumbnailFetcher(
                    data = data,
                    options = options,
                    connectionManager = connectionManager,
                )

                else -> null
            }
        }
    }

    protected companion object {

        const val SIZE_MAX = 500

        fun Uri.isSsh() = scheme == "ssh"

        fun Size.isThumbnail(): Boolean {
            return width is Dimension.Pixels &&
                    height is Dimension.Pixels &&
                    (width as Dimension.Pixels).px <= SIZE_MAX &&
                    (height as Dimension.Pixels).px <= SIZE_MAX
        }

        @Px
        fun Size.singleSize(): Int = maxOf(
            width.pxOrElse { -1 },
            height.pxOrElse { -1 }
        ).let { if (it <= 0) SIZE_MAX else it }
    }
}