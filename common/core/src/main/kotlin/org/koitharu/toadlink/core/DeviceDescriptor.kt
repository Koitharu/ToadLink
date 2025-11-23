package org.koitharu.toadlink.core

data class DeviceDescriptor(
    val id: Int,
    val hostname: String,
    val port: Int,
    val alias: String?,
    val username: String,
    val password: String,
) {

    val address = "$hostname:$port"

    val displayName: String
        get() = alias ?: hostname
}