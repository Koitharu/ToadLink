package org.koitharu.toadlink.utils.coil.thumbnails

import androidx.annotation.Px
import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Size
import coil3.size.pxOrElse
import coil3.util.MimeTypeMap
import okio.buffer
import org.koitharu.toadlink.client.SshConnection
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.files.fs.MimeType.Companion.toMimeTypeOrNull

class SshThumbnailFetcher(
    private val data: Uri,
    private val options: Options,
    private val registry: ThumbnailersRegistry,
    private val connectionManager: SshConnectionManager,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val mimeType = MimeTypeMap.getMimeTypeFromUrl(data.toString())?.toMimeTypeOrNull()
            ?: return null
        val thumbnailers = registry.getThumbnailers(mimeType)
        if (thumbnailers.isEmpty()) {
            return null
        }
        val connection = connectionManager.awaitConnection()
        return thumbnailers.firstNotNullOfOrNull { thumbnailer ->
            fetchFromThumbnailer(connection, thumbnailer)?.let { source ->
                SourceFetchResult(
                    source = ImageSource(
                        source = source.buffer(),
                        fileSystem = connection.fileSystem,
                    ),
                    mimeType = mimeType.toString(),
                    dataSource = DataSource.NETWORK,
                )
            }
        }
    }

    private suspend fun fetchFromThumbnailer(
        connection: SshConnection,
        thumbnailer: Thumbnailer,
    ) = runCatchingCancellable {
        val cmdline = thumbnailer.getCommandLine(
            inputPath = data.path ?: return@runCatchingCancellable null,
            size = options.size.singleSize()
        )
        connection.executeAsSource(cmdline)
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()

    class Factory(
        private val connectionManager: SshConnectionManager,
    ) : Fetcher.Factory<Uri> {

        private val registry = ThumbnailersRegistry(connectionManager)

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? = if (data.isSSH() && options.size.isThumbnail()) {
            SshThumbnailFetcher(
                data = data,
                options = options,
                registry = registry,
                connectionManager = connectionManager
            )
        } else {
            null
        }
    }

    private companion object {

        const val SIZE_MAX = 500

        fun Uri.isSSH() = scheme == "ssh"

        fun Size.isThumbnail(): Boolean {
            val maxDimension = maxOf(
                width.pxOrElse { -1 },
                height.pxOrElse { -1 }
            )
            return maxDimension in 1..SIZE_MAX
        }

        @Px
        fun Size.singleSize(): Int = maxOf(
            width.pxOrElse { -1 },
            height.pxOrElse { -1 }
        ).let { if (it <= 0) SIZE_MAX else it }
    }
}