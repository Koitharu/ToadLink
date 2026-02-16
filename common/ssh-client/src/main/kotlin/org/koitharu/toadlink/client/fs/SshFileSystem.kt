package org.koitharu.toadlink.client.fs

import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Source
import org.koitharu.toadlink.client.SshConnectionImpl
import org.koitharu.toadlink.client.executeBlocking
import org.koitharu.toadlink.core.util.escape
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.core.util.splitByWhitespace
import org.koitharu.toadlink.core.util.unescape

internal class SshFileSystem(
    private val connection: SshConnectionImpl,
) : FileSystem() {

    override fun appendingSink(file: Path, mustExist: Boolean) = SCPSink(
        connection = connection.connection,
        path = file.escaped(),
        append = true
    )

    override fun atomicMove(source: Path, target: Path) {
        connection.executeBlocking("mv -f -r -T ${source.escaped()} ${target.escaped()}")
    }

    override fun canonicalize(path: Path): Path {
        val escapedPath = path.escaped()
        return runCatchingCancellable {
            connection.executeBlocking("realpath $escapedPath")
        }.recoverCatching {
            connection.executeBlocking("readlink -f $escapedPath")
        }.map {
            it.toPath(normalize = false)
        }.getOrThrow()
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        val command = buildString {
            append("mkdir ")
            if (!mustCreate) {
                append("-p ")
            }
            append(dir.escaped())
        }
        connection.executeBlocking(command)
    }

    override fun createSymlink(source: Path, target: Path) {
        connection.executeBlocking("ln -s -f -T ${source.escaped()} ${target.escaped()}")
    }

    override fun delete(path: Path, mustExist: Boolean) {
        delete(path, mustExist, recursive = false)
    }

    override fun list(dir: Path): List<Path> {
        val command = buildString {
            append("ls -lQk1A --time-style=iso ")
            append(dir.escaped())
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

    override fun sink(file: Path, mustCreate: Boolean) = SCPSink(
        connection = connection.connection,
        path = file.escaped(),
        append = false
    )

    override fun source(file: Path): Source = SCPSource(connection.connection, file.escaped())

    override fun deleteRecursively(fileOrDirectory: Path, mustExist: Boolean) {
        delete(fileOrDirectory, mustExist, recursive = true)
    }

    override fun copy(source: Path, target: Path) {
        connection.executeBlocking("cp -r -f -T ${source.escaped()} ${target.escaped()}")
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
            append(path.escaped())
        }
        connection.executeBlocking(cmd)
    }

    private fun String.unquote() = removeSurrounding("\"")

    private fun Path.escaped() = toString().escape()
}