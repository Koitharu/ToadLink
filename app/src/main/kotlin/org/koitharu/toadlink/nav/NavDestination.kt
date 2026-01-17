package org.koitharu.toadlink.nav

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data object FindDeviceDestination : NavKey, Parcelable

@Parcelize
@Serializable
data class ControlDestination(
    val deviceId: Int,
) : NavKey, Parcelable
