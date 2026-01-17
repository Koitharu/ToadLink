package org.koitharu.toadlink.finddevice

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.NetworkDevice

@Immutable
data class FindDeviceState(
    val savedDevices: ImmutableList<DeviceDescriptor>,
    val availableDevices: ImmutableList<NetworkDevice>,
    val isScanning: Boolean,
    val connectedDevice: Int,
) {

    constructor() : this(
        savedDevices = persistentListOf(),
        availableDevices = persistentListOf(),
        isScanning = false,
        connectedDevice = 0,
    )
}