package org.koitharu.toadlink.actions.ui.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.core.RemoteAction

@Immutable
internal data class ActionsState(
    val actions: ImmutableList<RemoteAction>,
    val isLoading: Boolean,
) {

    constructor() : this(
        actions = persistentListOf(),
        isLoading = true,
    )
}
