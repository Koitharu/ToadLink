package org.koitharu.toadlink.core

import androidx.compose.runtime.Immutable

@Immutable
public class RemoteAction(
    public val id: Int,
    public val name: String,
    public val cmdline: String,
    public val isConfirmationRequired: Boolean,
)