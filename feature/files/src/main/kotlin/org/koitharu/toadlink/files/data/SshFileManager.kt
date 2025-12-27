package org.koitharu.toadlink.files.data

import android.webkit.MimeTypeMap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okio.Path
import okio.Path.Companion.toPath
import org.koitharu.toadlink.client.SshConnection
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.core.util.splitByWhitespace
import org.koitharu.toadlink.core.util.unescape
import org.koitharu.toadlink.files.fs.MimeType
import org.koitharu.toadlink.files.fs.MimeType.Companion.toMimeTypeOrNull
import org.koitharu.toadlink.files.fs.SshFile
import java.text.SimpleDateFormat
import java.util.EnumMap
import java.util.Locale

class SshFileManager(
    private val connection: SshConnection,
) {

    private val dateFormat = SimpleDateFormat("yyyy-mm-dd HH:MM", Locale.ROOT)

    suspend fun resolvePath(path: String): Path = runCatchingCancellable {
        connection.execute("realpath $path")
    }.recoverCatching {
        connection.execute("readlink -f $path")
    }.map {
        it.toPath(normalize = false)
    }.getOrThrow()

    suspend fun getUserHome(): Path = connection.execute("xdg-user-dir").toPath()

    suspend fun getXdgUserDir(dir: XdgUserDir): Path =
        connection.execute("xdg-user-dir ${dir.name}").toPath()

    suspend fun getXdgUserDirs(): Map<XdgUserDir, Path?> = coroutineScope {
        val homeDir = getUserHome()
        XdgUserDir.entries.map { xdgUserDir ->
            async { xdgUserDir to getXdgUserDir(xdgUserDir).takeUnless { it == homeDir } }
        }.awaitAll().toMap(EnumMap(XdgUserDir::class.java))
    }

    suspend fun getFileType(
        path: String,
    ): MimeType = connection.execute(
        "file -bNr --mime-type \"$path\""
    ).toMimeTypeOrNull() ?: MimeType.UNKNOWN

    suspend fun listFiles(
        path: Path,
        includeHidden: Boolean,
    ): ImmutableList<SshFile> {
        val command = buildString {
            append("ls -lQk1")
            if (includeHidden) {
                append('A')
            }
            append(" --time-style=long-iso --time=mtime \"")
            append(path)
            append('"')
        }
        return connection.execute(command)
            .lines()
            .mapNotNull { line ->
                line.parseFile(path)
            }.toPersistentList()
    }

    private fun String.parseFile(parentPath: Path): SshFile? {
        val parts = splitByWhitespace()
        if (parts.size < 8) {
            return null
        }
        val dateString = parts[5] + " " + parts[6]
        val name = parts[7].unquote().unescape()
        val isDirectory = parts[0].firstOrNull() == 'd'
        return SshFile(
            path = parentPath.resolve(name),
            size = parts[4].toLong(),
            lastModified = dateFormat.parse(dateString)?.time ?: 0L,
            owner = parts[2],
            symlinkTarget = if (parts[0].firstOrNull() == 'l') {
                parts.getOrNull(9)?.unquote()?.unescape()
            } else {
                null
            },
            type = if (isDirectory) {
                MimeType.DIRECTORY
            } else {
                getMimeType(name) ?: MimeType.UNKNOWN
            }
        )
    }

    private fun String.unquote() = removeSurrounding("\"")

    private fun getMimeType(fileName: String) = getNormalizedExtension(fileName)?.let {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
    }?.toMimeTypeOrNull()

    private fun getNormalizedExtension(name: String): String? = name
        .lowercase()
        .removeSuffix("~")
        .removeSuffix(".tmp")
        .substringAfterLast('.', "")
        .takeIf { it.length in 1..5 }
}