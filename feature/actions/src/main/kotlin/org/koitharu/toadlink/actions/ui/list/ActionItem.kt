package org.koitharu.toadlink.actions.ui.list

import org.koitharu.toadlink.actions.ui.ExecutionState
import org.koitharu.toadlink.core.RemoteAction

internal data class ActionItem(
    val action: RemoteAction,
    val state: ExecutionState,
)