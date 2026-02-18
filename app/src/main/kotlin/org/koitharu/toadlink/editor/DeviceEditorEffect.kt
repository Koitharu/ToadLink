package org.koitharu.toadlink.editor

sealed interface DeviceEditorEffect {

    data class OnError(val error: Throwable): DeviceEditorEffect

    data class OpenDevice(val deviceId: Int): DeviceEditorEffect

    data object Back: DeviceEditorEffect
}