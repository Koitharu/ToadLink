package org.koitharu.toadlink.actions.ui.list

import androidx.compose.runtime.Immutable
import org.koitharu.toadlink.actions.ui.ExecutionState
import org.koitharu.toadlink.core.RemoteAction

@Immutable
internal data class ActionItem(
    val action: RemoteAction,
    val state: ExecutionState,
)