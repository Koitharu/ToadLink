package org.koitharu.toadlink.utils.coil.thumbnails

import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.Path.Companion.toPath
import org.koitharu.toadlink.client.SshConnection
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.util.escape
import org.koitharu.toadlink.core.util.nullIfEmpty
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.files.fs.MimeType

class SshFileIconFetcher(
    private val data: Uri,
    private val options: Options,
    private val connectionManager: SshConnectionManager,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val host = data.authority ?: return null
        val path = data.path ?: return null
        val connection = connectionManager.peekConnection(host) ?: return null
        val iconName = connection.execute(
            "gio info -a standard::icon %s | grep \"standard::icon\" | awk '{print \$2}' | tr -d ','".format(
                path.escape()
            )
        ).nullIfEmpty() ?: return null
        val iconTheme = connection.getIconTheme()
        val icons = connection.execute(
            "find /usr/share/icons/%s/ ~/.local/share/icons/%s/ -name \"%s.*\""
                .format(iconTheme, iconTheme, iconName)
        ).lines()
        return icons.firstNotNullOfOrNull {
            SourceFetchResult(
                source = ImageSource(it.toPath(), connection.fileSystem),
                mimeType = MimeType.fromFileName(it)?.toString(),
                dataSource = DataSource.NETWORK,
            )
        }
    }

    private suspend fun SshConnection.getIconTheme() = runCatchingCancellable {
        execute("gsettings get org.gnome.desktop.interface icon-theme | tr -d \"'\"")
    }.recoverCatching {
        execute("xfconf-query -c xsettings -p /Net/IconThemeName")
    }.recoverCatching {
        execute("kreadconfig6 --group Icons --key Theme")
    }.getOrNull()

    class Factory(
        private val connectionManager: SshConnectionManager,
    ) : Fetcher.Factory<Uri> {

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? = if (data.scheme == "ssh") {
            SshFileIconFetcher(
                data = data,
                options = options,
                connectionManager = connectionManager
            )
        } else {
            null
        }
    }
}