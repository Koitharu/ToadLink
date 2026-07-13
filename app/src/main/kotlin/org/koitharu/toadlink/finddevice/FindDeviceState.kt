package org.koitharu.toadlink.finddevice

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.NetworkDevice

@Immutable
data class FindDeviceState(
    val savedDevices: ImmutableList<DeviceDescriptor>,
    val availableDevices: ImmutableList<NetworkDevice>,
    val isScanning: Boolean,
    val connectedDevices: ImmutableSet<Int>,
) {

    constructor() : this(
        savedDevices = persistentListOf(),
        availableDevices = persistentListOf(),
        isScanning = false,
        connectedDevices = persistentSetOf(),
    )
}