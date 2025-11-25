package org.koitharu.toadlink.adddevice

import androidx.annotation.StringRes

data class AddDeviceState(
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

    constructor(initialAddress: String?) : this(
        isLoading = false,
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