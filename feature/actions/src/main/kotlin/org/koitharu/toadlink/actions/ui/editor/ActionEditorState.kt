package org.koitharu.toadlink.actions.ui.editor

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.core.RemoteAction

@Immutable
internal data class ActionEditorState(
    val actionId: Int,
    val name: String,
    val cmdline: TextFieldValue,
    val cmdlineCompletion: ImmutableList<String>,
    val isLoading: Boolean,
) {

    val isSaveEnabled: Boolean
        get() = name.isNotEmpty() && cmdline.text.isNotEmpty()

    constructor(action: RemoteAction?) : this(
        actionId = action?.id ?: 0,
        name = action?.name.orEmpty(),
        cmdline = TextFieldValue(action?.cmdline.orEmpty()),
        cmdlineCompletion = persistentListOf(),
        isLoading = false,
    )
}