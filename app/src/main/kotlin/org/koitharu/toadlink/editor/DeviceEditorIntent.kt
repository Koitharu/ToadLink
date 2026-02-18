package org.koitharu.toadlink.editor

sealed interface DeviceEditorIntent {

    data class UpdateHostname(val value: String) : DeviceEditorIntent

    data class UpdatePort(val value: Int) : DeviceEditorIntent

    data class UpdateUsername(val value: String) : DeviceEditorIntent

    data class UpdatePassword(val value: String) : DeviceEditorIntent

    data class UpdateAlias(val value: String) : DeviceEditorIntent

    data object ToggleConnectNow : DeviceEditorIntent

    data object SaveDevice : DeviceEditorIntent
}