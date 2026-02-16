package org.koitharu.toadlink.client.fs

import com.trilead.ssh2.Connection
import com.trilead.ssh2.Session
import okio.Buffer
import okio.Source
import okio.Timeout

internal class SCPSource(
    private val connection: Connection,
    private val path: String,
) : Source {

    private var session: Session? = null
    private var fileName: String? = null
    private var available = -1L

    override fun read(sink: Buffer, byteCount: Long): Long {
        val s = session ?: openSession()
        if (available <= 0) {
            return -1
        }
        val buffer = ByteArray(byteCount.toInt())
        val bytesRead = s.stdout.read(buffer)
        sink.write(buffer, 0, bytesRead)
        available -= bytesRead
        if (available <= 0) {
            s.stdin.buffered().run {
                write(0x0)
                flush()
            }
        }
        return bytesRead.toLong()
    }

    override fun timeout(): Timeout = Timeout.NONE

    override fun close() {
        session?.close()
        session = null
    }

    private fun openSession() = connection.openSession().also {
        session = it
        it.execCommand("scp -f $path")
        val output = it.stdin.buffered()
        val input = it.stdout.bufferedReader()

        output.write(0x0)
        output.flush()

        //read header
        while (true) {
            val c = input.read()
            if (c < 0) {
                throw SCPException("Remote scp terminated unexpectedly.")
            }
            val line = input.readLine()
            if ((c == 1) || (c == 2)) {
                throw SCPException("Remote SCP error: $line")
            }
            when (c) {
                'T'.code -> continue
                'C'.code -> {
                    parseCLine(line)
                    break
                }

                else -> throw SCPException("Remote SCP error: " + (c.toChar()) + line)
            }
        }
        output.write(0x0)
        output.flush()
    }

    private fun parseCLine(line: String) {
        /* Minimum line: "xxxx y z" ---> 8 chars */

        if (line.length < 8) {
            throw SCPException("Malformed C line sent by remote SCP binary, line too short.")
        }

        if ((line[4] != ' ') || (line[5] == ' ')) {
            throw SCPException("Malformed C line sent by remote SCP binary.")
        }

        val lengthNameSep = line.indexOf(' ', 5)

        if (lengthNameSep == -1) {
            throw SCPException("Malformed C line sent by remote SCP binary.")
        }

        val lengthSubstring = line.substring(5, lengthNameSep)
        val nameSubstring = line.substring(lengthNameSep + 1)

        if ((lengthSubstring.isEmpty()) || (nameSubstring.isEmpty())) {
            throw SCPException("Malformed C line sent by remote SCP binary.")
        }

        if ((6 + lengthSubstring.length + nameSubstring.length) != line.length) throw SCPException(
            "Malformed C line sent by remote SCP binary."
        )

        try {
            available = lengthSubstring.toLong()
        } catch (e: NumberFormatException) {
            throw SCPException(
                "Malformed C line sent by remote SCP binary, cannot parse file length.",
                e
            )
        }

        if (available < 0) {
            throw SCPException("Malformed C line sent by remote SCP binary, illegal file length.")
        }

        fileName = nameSubstring
    }
}