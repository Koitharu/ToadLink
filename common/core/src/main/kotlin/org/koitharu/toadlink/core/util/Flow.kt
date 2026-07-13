package org.koitharu.toadlink.core.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.Duration

public fun tickerFlow(delay: Duration): Flow<Long> = channelFlow {
    while (isActive && !trySend(System.currentTimeMillis()).isClosed) {
        delay(delay)
    }
}

public fun <T> Flow<T>.chunked(size: Int): Flow<List<T>> = flow {
    var accumulator = ArrayList<T>(size)
    collect { value ->
        accumulator.add(value)
        if (accumulator.size == size) {
            emit(accumulator)
            accumulator = ArrayList(size)
        }
    }
}

public suspend fun <T : Any> Flow<T?>.firstNotNull(): T = filterNotNull().first()

public fun <T> Flow<T>.logErrors(): Flow<T> = catch { e ->
    e.printStackTrace()
    throw e
}
