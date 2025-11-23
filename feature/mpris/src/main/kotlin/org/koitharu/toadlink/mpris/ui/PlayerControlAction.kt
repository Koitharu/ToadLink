package org.koitharu.toadlink.mpris.ui

sealed interface PlayerControlAction {

    data object Play : PlayerControlAction

    data object Pause : PlayerControlAction

    data object PlayPause : PlayerControlAction

    data object Prev : PlayerControlAction

    data object Next : PlayerControlAction

    data class Rewind(val delta: Int) : PlayerControlAction

    data class Seek(val position: Int) : PlayerControlAction
}