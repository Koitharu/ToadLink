package org.koitharu.toadlink.mpris

import androidx.compose.runtime.Immutable

@Immutable
data class PlayerName(
    val dbusName: String,
    val appName: String?,
    val icon: String?,
)