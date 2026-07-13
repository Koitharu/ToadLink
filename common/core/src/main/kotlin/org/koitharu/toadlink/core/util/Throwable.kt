package org.koitharu.toadlink.core.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

@Suppress("WRONG_INVOCATION_KIND")
@OptIn(ExperimentalContracts::class)
public inline fun <T, R> T.runCatchingCancellable(block: T.() -> R): Result<R> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        Result.success(block())
    } catch (e: InterruptedException) {
        throw e
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

public inline fun <R, T : R> Result<T>.recoverCatchingCancellable(transform: (exception: Throwable) -> R): Result<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> runCatchingCancellable { transform(exception) }
    }
}