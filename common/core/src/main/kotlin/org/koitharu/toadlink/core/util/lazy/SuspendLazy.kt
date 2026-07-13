package org.koitharu.toadlink.core.util.lazy

import org.koitharu.toadlink.core.util.runCatchingCancellable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal typealias SuspendLazyInitializer<T> = suspend () -> T

public interface SuspendLazy<T> {

    public val isInitialized: Boolean

    public suspend fun get(): T

    public fun peek(): T?
}

public suspend fun <T> SuspendLazy<T>.getOrNull(): T? = runCatchingCancellable {
    get()
}.getOrNull()

public suspend fun <R, T : R> SuspendLazy<T>.getOrDefault(defaultValue: R): R = runCatchingCancellable {
    get()
}.getOrDefault(defaultValue)

public fun <T> suspendLazy(
    context: CoroutineContext = EmptyCoroutineContext,
    initializer: SuspendLazyInitializer<T>,
): SuspendLazy<T> = SuspendLazyImpl(context, initializer)

