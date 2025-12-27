package org.koitharu.toadlink.client.fs

import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import org.koitharu.toadlink.client.SshConnectionImpl
import org.koitharu.toadlink.client.executeBlocking
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.core.util.splitByWhitespace
import org.koitharu.toadlink.core.util.unescape

internal class SshFileSystem(
    private val connection: SshConnectionImpl,
) : FileSystem() {

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun atomicMove(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun canonicalize(path: Path): Path = runCatchingCancellable {
        connection.executeBlocking("realpath $path")
    }.recoverCatching {
        connection.executeBlocking("readlink -f $path")
    }.map {
        it.toPath(normalize = false)
    }.getOrThrow()

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        TODO("Not yet implemented")
    }

    override fun createSymlink(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun delete(path: Path, mustExist: Boolean) {
        delete(path, mustExist, recursive = false)
    }

    override fun list(dir: Path): List<Path> {
        val command = buildString {
            append("ls -lQk1A \"")
            append(dir.toString())
            append('"')
        }
        return connection.executeBlocking(command)
            .lines()
            .mapNotNull { line ->
                val parts = line.splitByWhitespace()
                val name = (parts.getOrNull(7) ?: return@mapNotNull null).unquote().unescape()
                dir.div(name)
            }
    }

    override fun listOrNull(dir: Path): List<Path>? = runCatchingCancellable {
        list(dir)
    }.getOrNull()

    override fun metadataOrNull(path: Path): FileMetadata? = null

    override fun openReadOnly(file: Path): FileHandle {
        throw UnsupportedOperationException("Not supported for remote files")
    }

    override fun openReadWrite(
        file: Path,
        mustCreate: Boolean,
        mustExist: Boolean,
    ): FileHandle {
        throw UnsupportedOperationException("Not supported for remote files")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun source(file: Path): Source = SCPSource(connection.connection, file.toString())

    override fun deleteRecursively(fileOrDirectory: Path, mustExist: Boolean) {
        delete(fileOrDirectory, mustExist, recursive = true)
    }

    override fun copy(source: Path, target: Path) {
        connection.executeBlocking("cp -r $source $target")
    }

    private fun delete(path: Path, mustExist: Boolean, recursive: Boolean) {
        val cmd = buildString {
            append("rm -d ")
            if (mustExist) {
                append("--interactive=never ")
            } else {
                append("-f ")
            }
            if (recursive) {
                append("-r ")
            }
            append('"')
            append(path)
            append('"')
        }
        connection.executeBlocking(cmd)
    }

    private fun String.unquote() = removeSurrounding("\"")
}