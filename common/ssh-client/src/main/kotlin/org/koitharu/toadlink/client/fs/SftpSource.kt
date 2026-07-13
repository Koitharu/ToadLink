package org.koitharu.toadlink.client.fs

import kotlinx.coroutines.runBlocking
import okio.Buffer
import okio.Source
import okio.Timeout
import org.connectbot.sshlib.SftpClient
import org.connectbot.sshlib.SftpOpenFlag
import org.connectbot.sshlib.getOrThrow
import org.koitharu.toadlink.core.util.lazy.suspendLazy
import java.util.EnumSet

internal class SftpSource(
    private val client: SftpClient,
    private val path: String,
) : Source {

    private val fileHandle = suspendLazy {
        client.open(path, EnumSet.of(SftpOpenFlag.READ)).getOrThrow()
    }
    private var offset = 0L

    override fun close() {
        fileHandle.peek()?.let {
            runBlocking { client.close(it) }
        }
        client.close()
    }

    override fun read(sink: Buffer, byteCount: Long): Long = runBlocking {
        val handle = fileHandle.get()
        val bytes = client.read(handle, offset, byteCount.toInt()).getOrThrow()
        if (bytes == null) {
            -1
        } else {
            offset += bytes.size
            sink.write(bytes)
            bytes.size.toLong()
        }
    }

    override fun timeout(): Timeout = Timeout.NONE
}