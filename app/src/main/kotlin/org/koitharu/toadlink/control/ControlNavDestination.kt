package org.koitharu.toadlink.control

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ControlNavDestination : NavKey {

    @Serializable
    data object Actions : ControlNavDestination

    @Serializable
    data object MPRIS : ControlNavDestination

    @Serializable
    data object Files : ControlNavDestination
}