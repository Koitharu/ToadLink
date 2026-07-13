package org.koitharu.toadlink.client.fs

import okio.ForwardingSource
import okio.Source

private class ExtraCloseableSource(
    delegate: Source,
    private val extraCloseable: AutoCloseable,
) : ForwardingSource(delegate) {

    override fun close() {
        var error: Throwable? = null
        try {
            delegate.close()
        } catch (e: Throwable) {
            error = e
            throw e
        } finally {
            extraCloseable.closeFinally(error)
        }
    }
}

private fun AutoCloseable.closeFinally(cause: Throwable?): Unit = when {
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}

internal fun Source.withExtraCloseable(closeable: AutoCloseable): Source = ExtraCloseableSource(
    delegate = this,
    extraCloseable = closeable
)