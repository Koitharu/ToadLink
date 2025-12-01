package org.koitharu.toadlink.actions.ui.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class ActionsState(
    val actions: PersistentList<ActionItem>,
    val isLoading: Boolean,
) {

    constructor() : this(
        actions = persistentListOf(),
        isLoading = true,
    )
}
