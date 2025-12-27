package org.koitharu.toadlink.utils.coil

import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.util.MimeTypeMap
import org.koitharu.toadlink.client.SshConnectionManager

class SshImageFetcher internal constructor(
    private val data: Uri,
    private val options: Options,
    private val connectionManager: SshConnectionManager,
) : Fetcher {

    @OptIn(coil3.annotation.InternalCoilApi::class)
    override suspend fun fetch(): FetchResult? {
        val connection = connectionManager.awaitConnection()
        val content = connection.getFileContent(
            path = data.path ?: return null,
        )
        return SourceFetchResult(
            source = ImageSource(
                source = content,
                fileSystem = options.fileSystem,
                metadata = null,
            ),
            mimeType = MimeTypeMap.getMimeTypeFromUrl(data.toString()),
            dataSource = DataSource.NETWORK,
        )
    }

    class Factory(
        private val connectionManager: SshConnectionManager,
    ) : Fetcher.Factory<Uri> {

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? = if (data.scheme == "ssh") {
            SshImageFetcher(
                data = data,
                options = options,
                connectionManager = connectionManager,
            )
        } else {
            null
        }
    }
}