package org.koitharu.toadlink.control

import androidx.compose.runtime.Immutable
import org.koitharu.toadlink.core.DeviceDescriptor

@Immutable
sealed interface ControlState {

    val device: DeviceDescriptor?

    data class Connecting(
        override val device: DeviceDescriptor?,
    ) : ControlState

    data class Error(
        override val device: DeviceDescriptor?,
        val error: Throwable,
    ) : ControlState

    data class Connected(
        override val device: DeviceDescriptor,
        val section: ControlSection,
    ) : ControlState
}