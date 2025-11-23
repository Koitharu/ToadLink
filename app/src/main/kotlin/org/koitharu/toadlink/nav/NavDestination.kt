package org.koitharu.toadlink.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object FindDeviceDestination : NavKey

@Serializable
data class ControlDestination(
    val deviceId: Int,
) : NavKey

@Serializable
data class AddDeviceDestination(
    val initialAddress: String?
) : NavKey