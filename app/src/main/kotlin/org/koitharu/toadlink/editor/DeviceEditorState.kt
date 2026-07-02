package org.koitharu.toadlink.editor

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import org.koitharu.toadlink.core.DeviceDescriptor

@Immutable
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
    val key: String,
    val alias: String,
    val connectNow: Boolean,
    val autoConnect: Boolean,
) {

    constructor(device: DeviceDescriptor) : this(
        isNewDevice = false,
        isLoading = false,
        hostname = device.hostname,
        port = device.port,
        username = device.username,
        password = device.password,
        key = device.key.orEmpty(),
        hostnameError = 0,
        portError = 0,
        usernameError = 0,
        alias = device.alias.orEmpty(),
        connectNow = false,
        autoConnect = device.connectAutomatically,
    )

    constructor(initialAddress: String?, isNewDevice: Boolean) : this(
        isNewDevice = isNewDevice,
        isLoading = !isNewDevice,
        hostname = initialAddress?.substringBeforeLast(':').orEmpty(),
        port = initialAddress?.substringAfterLast(':', "")?.toIntOrNull() ?: 22,
        username = "",
        password = "",
        key = "",
        hostnameError = 0,
        portError = 0,
        usernameError = 0,
        alias = "",
        connectNow = true,
        autoConnect = false,
    )

    fun hasErrors() = portError != 0 || hostnameError != 0 || usernameError != 0

    fun isDoneEnabled() = !isLoading &&
            hostname.isNotEmpty() &&
            port > 0 &&
            username.isNotEmpty() &&
            password.isNotEmpty()
}