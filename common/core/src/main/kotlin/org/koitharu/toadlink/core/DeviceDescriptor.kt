package org.koitharu.toadlink.core

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime

@Immutable
data class DeviceDescriptor(
    val id: Int,
    val hostname: String,
    val port: Int,
    val alias: String?,
    val username: String,
    val password: String,
    val key: String?,
    val lastConnect: LocalDateTime?,
    val connectAutomatically: Boolean,
) {

    val address = "$hostname:$port"

    val displayName: String
        get() = alias ?: hostname
}