package org.koitharu.toadlink.control

sealed interface ControlIntent {

    data class SwitchSection(
        val section: ControlSection,
    ) : ControlIntent
}