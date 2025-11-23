package org.koitharu.toadlink.mpris.ui

internal interface PlayerControlEffect {

    data class OnError(
        val error: Throwable,
    ) : PlayerControlEffect
}