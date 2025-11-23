package org.koitharu.toadlink.ui.mvi

fun interface MviIntentHandler<in I> {

    fun handleIntent(intent: I)

    operator fun invoke(intent: I) = handleIntent(intent)

    companion object {

        val NO_OP = MviIntentHandler<Any> { /* no-op */ }
    }
}