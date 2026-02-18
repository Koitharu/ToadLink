package org.koitharu.toadlink.finddevice

sealed interface FindDeviceIntent {

    data object Refresh : FindDeviceIntent

    data class Remove(val deviceId: Int) : FindDeviceIntent

    data class Disconnect(val deviceId: Int) : FindDeviceIntent
}