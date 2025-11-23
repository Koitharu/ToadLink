package org.koitharu.toadlink.adddevice

data class AddDeviceState(
    val isLoading: Boolean,
    val hostname: String,
    val port: Int,
    val username: String,
    val password: String,
) {

    constructor(initialAddress: String?) : this(
        isLoading = false,
        hostname = initialAddress?.substringBeforeLast(':').orEmpty(),
        port = initialAddress?.substringAfterLast(':', "")?.toIntOrNull() ?: 22,
        username = "",
        password = "",
    )
}