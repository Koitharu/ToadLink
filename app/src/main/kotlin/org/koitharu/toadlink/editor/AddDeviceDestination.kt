package org.koitharu.toadlink.editor

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class AddDeviceDestination(
    val initialAddress: String?,
) : NavKey