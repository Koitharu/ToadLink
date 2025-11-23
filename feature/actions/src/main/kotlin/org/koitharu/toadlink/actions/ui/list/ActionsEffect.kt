package org.koitharu.toadlink.actions.ui.list

internal sealed interface ActionsEffect {

    data class OnError(
        val error: Throwable,
    ) : ActionsEffect
}