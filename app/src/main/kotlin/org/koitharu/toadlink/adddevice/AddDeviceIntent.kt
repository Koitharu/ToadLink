package org.koitharu.toadlink.adddevice

sealed interface AddDeviceIntent {

    data class UpdateHostname(val value: String) : AddDeviceIntent

    data class UpdatePort(val value: Int) : AddDeviceIntent

    data class UpdateUsername(val value: String) : AddDeviceIntent

    data class UpdatePassword(val value: String) : AddDeviceIntent

    data object SaveDevice : AddDeviceIntent
}