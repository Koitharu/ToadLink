package org.koitharu.toadlink.utils.coil.thumbnails

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.Path.Companion.toPath
import okio.buffer
import org.koitharu.toadlink.client.SshConnection
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.client.tryExecute
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.files.fs.MimeType
import org.koitharu.toadlink.files.fs.MimeType.Companion.toMimeTypeOrNull

class ThumbnailersRegistry(
    private val connectionManager: SshConnectionManager,
) {

    private val fetchMutex = Mutex()
    private var thumbnailers: List<Thumbnailer>? = null
    private var deviceId: Int = 0

    suspend fun getThumbnailers(mimeType: MimeType): List<Thumbnailer> {
        val connection = connectionManager.awaitConnection()
        val list = fetchMutex.withLock {
            if (thumbnailers == null || deviceId != connection.host.id) {
                thumbnailers = connection.fetchThumbnailers()
                deviceId = connection.host.id
                thumbnailers
            } else {
                thumbnailers
            }
        }
        return list?.filter { it.isSupported(mimeType) }.orEmpty()
    }

    private suspend fun SshConnection.fetchThumbnailers(): List<Thumbnailer> =
        withContext(Dispatchers.Default) {
            val fs = fileSystem
            val files = runInterruptible(Dispatchers.IO) { fs.list(PATH.toPath()) }
            val result = ArrayList<Thumbnailer>(files.size + GenericThumbnailers.size)
            files.mapNotNullTo(result) { path ->
                fs.source(path).buffer().use {
                    it.parseThumbnailer()?.takeIf { isSupported(it) }
                }
            }
            GenericThumbnailers.filterTo(result) {
                isSupported(it)
            }
            result
        }

    private suspend fun SshConnection.isSupported(thumbnailer: Thumbnailer): Boolean {
        val cmd = thumbnailer.tryExec ?: return true
        return !tryExecute("command -v \"$cmd\"").getOrNull().isNullOrBlank()
    }

    private fun BufferedSource.parseThumbnailer(): Thumbnailer? = runCatchingCancellable {
        var tryExec: String? = null
        var exec: String? = null
        var mimeType: String? = null
        while (true) {
            val line = this.readUtf8Line() ?: break
            when (line.substringBefore('=').trim()) {
                "TryExec" -> tryExec = line.substringAfter('=').trim()
                "Exec" -> exec = line.substringAfter('=').trim()
                "MimeType" -> mimeType = line.substringAfter('=').trim()
                else -> continue
            }
        }
        if (exec.isNullOrEmpty() || mimeType.isNullOrEmpty()) {
            return null
        }
        Thumbnailer(
            tryExec = tryExec,
            exec = exec,
            mimeTypes = mimeType.split(';').mapNotNull { it.toMimeTypeOrNull() }
        )
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()

    private companion object {

        val ImageMagick = Thumbnailer(
            tryExec = "convert",
            exec = "convert %u -thumbnail %s %o",
            mimeTypes = listOf(MimeType("image/*"))
        )

        val ImageMagickPdf = Thumbnailer(
            tryExec = "convert",
            exec = "convert %u[0] -thumbnail %s -background white -alpha remove PNG:%o",
            mimeTypes = listOf(MimeType("application/pdf"))
        )

        /**
         * List of generic thumbnailers that usually does not exist in /usr/bin/thumbnailers
         * However can be used on most desktop setups as fallback
         */
        val GenericThumbnailers = listOf(
            ImageMagick,
            ImageMagickPdf,
        )

        const val PATH = "/usr/share/thumbnailers"
    }
}