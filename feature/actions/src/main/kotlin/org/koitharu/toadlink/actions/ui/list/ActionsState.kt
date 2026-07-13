package org.koitharu.toadlink.actions.ui.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.core.DeviceDescriptor

@Immutable
internal data class ActionsState(
    val host: DeviceDescriptor,
    val actions: PersistentList<ActionItem>,
    val isLoading: Boolean,
) {

    constructor(
        host: DeviceDescriptor,
    ) : this(
        host = host,
        actions = persistentListOf(),
        isLoading = true,
    )
}
