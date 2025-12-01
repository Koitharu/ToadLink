package org.koitharu.toadlink.actions.ui.editor

internal sealed interface ActionEditorEffect {

    data object Close : ActionEditorEffect

    data class OnError(val error: Throwable) : ActionEditorEffect
}