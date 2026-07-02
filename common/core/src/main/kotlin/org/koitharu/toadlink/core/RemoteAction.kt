package org.koitharu.toadlink.core

import androidx.compose.runtime.Immutable

@Immutable
class RemoteAction(
	val id: Int,
	val name: String,
	val cmdline: String,
	val isConfirmationRequired: Boolean,
)