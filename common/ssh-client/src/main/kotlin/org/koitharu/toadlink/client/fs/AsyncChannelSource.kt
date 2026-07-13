package org.koitharu.toadlink.client.fs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import okio.Buffer
import okio.ForwardingSource
import okio.Pipe

internal class AsyncChannelSource private constructor(
    private val channel: ReceiveChannel<ByteArray>,
    private val pipe: Pipe,
) : ForwardingSource(pipe.source) {

    constructor(
        channel: ReceiveChannel<ByteArray>,
        maxBufferSize: Long = 64 * 1024,
    ) : this(channel, Pipe(maxBufferSize))

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            pipe.sink.use { sink ->
                for (chunk in channel) {
                    sink.write(Buffer().write(chunk), chunk.size.toLong())
                }
            }
        }
    }

    override fun close() {
        scope.cancel()
        pipe.cancel()
        channel.cancel()
    }
}