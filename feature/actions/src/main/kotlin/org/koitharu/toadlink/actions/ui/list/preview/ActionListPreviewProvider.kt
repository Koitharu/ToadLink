package org.koitharu.toadlink.actions.ui.list.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.actions.ui.ExecutionState
import org.koitharu.toadlink.actions.ui.list.ActionItem
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.RemoteAction

internal class ActionListPreviewProvider : PreviewParameterProvider<PersistentList<ActionItem>> {

    override val values: Sequence<PersistentList<ActionItem>>
        get() = sequence {
            val host = DeviceDescriptor(
                id = 1,
                hostname = "192.168.8.77",
                port = 22,
                alias = null,
                username = "user",
                password = "passw",
                key = null,
                lastConnect = null,
                connectAutomatically = false,
            )
            yield(
                persistentListOf(
                    ActionItem(
                        host,
                        RemoteAction(1, "Shutdown", "stub", false),
                        ExecutionState.None
                    ),
                    ActionItem(
                        host,
                        RemoteAction(2, "Reboot", "stub", false),
                        ExecutionState.Running
                    ),
                    ActionItem(
                        host = host,
                        action = RemoteAction(3, "Failed action", "stub", false),
                        state = ExecutionState.Failed(
                            RuntimeException(
                                "No such file or directory"
                            )
                        )
                    ),
                    ActionItem(
                        host = host,
                        action = RemoteAction(4, "Success action", "stub", false),
                        state = ExecutionState.Success(LoremIpsum(12).values.joinToString(" ")),
                    ),
                )
            )
        }
}