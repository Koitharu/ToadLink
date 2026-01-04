package org.koitharu.toadlink.control

sealed interface ControlEffect {

    data object CloseScreen : ControlEffect
}