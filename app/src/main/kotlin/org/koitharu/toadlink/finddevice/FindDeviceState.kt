package org.koitharu.toadlink.finddevice

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.core.DeviceDescriptor

@Immutable
data class FindDeviceState(
    val savedDevices: ImmutableList<DeviceDescriptor>,
    val availableDevices: ImmutableList<String>,
    val isScanning: Boolean,
    val connectedDevice: Int,
) {

    constructor() : this(
        savedDevices = persistentListOf(),
        availableDevices = persistentListOf(),
        isScanning = false,
        connectedDevice = -1,
    )
}