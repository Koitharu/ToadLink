package org.koitharu.toadlink.editor

import androidx.annotation.StringRes
import org.koitharu.toadlink.core.DeviceDescriptor

data class DeviceEditorState(
    val isNewDevice: Boolean,
    val isLoading: Boolean,
    val hostname: String,
    @field:StringRes
    val hostnameError: Int,
    val port: Int,
    @field:StringRes
    val portError: Int,
    val username: String,
    @field:StringRes
    val usernameError: Int,
    val password: String,
) {

    constructor(device: DeviceDescriptor) : this(
        isNewDevice = false,
        isLoading = false,
        hostname = device.hostname,
        port = device.port,
        username = device.username,
        password = device.password,
        hostnameError = 0,
        portError = 0,
        usernameError = 0,
    )

    constructor(initialAddress: String?, isNewDevice: Boolean) : this(
        isNewDevice = isNewDevice,
        isLoading = !isNewDevice,
        hostname = initialAddress?.substringBeforeLast(':').orEmpty(),
        port = initialAddress?.substringAfterLast(':', "")?.toIntOrNull() ?: 22,
        username = "",
        password = "",
        hostnameError = 0,
        portError = 0,
        usernameError = 0,
    )

    fun hasErrors() = portError != 0 || hostnameError != 0 || usernameError != 0
}