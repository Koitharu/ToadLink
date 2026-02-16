package org.koitharu.toadlink.client.fs

import com.trilead.ssh2.Connection
import com.trilead.ssh2.Session
import okio.Buffer
import okio.Sink
import okio.Timeout

internal class SCPSink(
    private val connection: Connection,
    private val path: String,
    private val append: Boolean,
) : Sink {

    private var session: Session? = null

    override fun close() {
        session?.close()
        session = null
    }

    override fun flush() {
        session?.stdin?.flush()
    }

    override fun timeout(): Timeout = Timeout.NONE

    override fun write(source: Buffer, byteCount: Long) {
        TODO()
    }
}
