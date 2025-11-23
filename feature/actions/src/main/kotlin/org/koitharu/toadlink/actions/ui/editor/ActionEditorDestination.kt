package org.koitharu.toadlink.actions.ui.editor

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koitharu.toadlink.core.RemoteAction

@Serializable
data class ActionEditorDestination(
    val action: RemoteAction?,
) : NavKey