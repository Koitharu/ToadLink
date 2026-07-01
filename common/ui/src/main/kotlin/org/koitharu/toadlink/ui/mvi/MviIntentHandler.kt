package org.koitharu.toadlink.ui.mvi

import androidx.compose.runtime.Stable

@Stable
fun interface MviIntentHandler<in I> {

    fun handleIntent(intent: I)

    operator fun invoke(intent: I) = handleIntent(intent)

    companion object {

        val NoOp = MviIntentHandler<Any> { /* no-op */ }
    }
}