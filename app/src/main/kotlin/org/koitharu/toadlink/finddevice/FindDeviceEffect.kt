package org.koitharu.toadlink.finddevice

sealed interface FindDeviceEffect {

    data class OnError(val error: Throwable): FindDeviceEffect

    data class OpenDevice(val deviceId: Int): FindDeviceEffect
}