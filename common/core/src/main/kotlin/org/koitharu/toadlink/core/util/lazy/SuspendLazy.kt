package org.koitharu.toadlink.core.util.lazy

import org.koitharu.toadlink.core.util.runCatchingCancellable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal typealias SuspendLazyInitializer<T> = suspend () -> T

interface SuspendLazy<T> {

    val isInitialized: Boolean

    suspend fun get(): T

    fun peek(): T?
}

suspend fun <T> SuspendLazy<T>.getOrNull(): T? = runCatchingCancellable {
    get()
}.getOrNull()

suspend fun <R, T : R> SuspendLazy<T>.getOrDefault(defaultValue: R): R = runCatchingCancellable {
    get()
}.getOrDefault(defaultValue)

fun <T> suspendLazy(
    context: CoroutineContext = EmptyCoroutineContext,
    initializer: SuspendLazyInitializer<T>,
): SuspendLazy<T> = SuspendLazyImpl(context, initializer)

