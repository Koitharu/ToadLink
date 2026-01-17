package org.koitharu.toadlink.finddevice

sealed interface FindDeviceIntent {

    data object Refresh : FindDeviceIntent
}