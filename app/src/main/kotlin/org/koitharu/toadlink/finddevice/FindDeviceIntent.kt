package org.koitharu.toadlink.finddevice

import org.koitharu.toadlink.core.DeviceDescriptor

sealed interface FindDeviceIntent {

    data class Connect(val device: DeviceDescriptor): FindDeviceIntent

    data object Refresh: FindDeviceIntent
}