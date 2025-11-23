package org.koitharu.toadlink.actions.ui.list

import org.koitharu.toadlink.core.RemoteAction

internal sealed interface ActionsIntent {

    data class Execute(
        val action: RemoteAction,
    ) : ActionsIntent
}