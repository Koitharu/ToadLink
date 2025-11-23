package org.koitharu.toadlink.actions.ui.editor

import androidx.compose.ui.text.input.TextFieldValue

internal sealed interface ActionEditorIntent {

    data class OnNameChanged(
        val value: String,
    ) : ActionEditorIntent

    data class OnCmdlineChanged(
        val value: TextFieldValue,
    ) : ActionEditorIntent

    data class ApplyCompletion(
        val value: String,
    ) : ActionEditorIntent

    data object Save : ActionEditorIntent
}