package org.koitharu.toadlink.core

import androidx.compose.runtime.Immutable

@Immutable
data class NetworkDevice(
    val address: String,
    val description: String?,
)