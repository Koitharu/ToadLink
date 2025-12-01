package org.koitharu.toadlink.actions.ui

internal sealed interface ExecutionState {

    data object None : ExecutionState

    data object Running : ExecutionState

    data class Failed(val error: Throwable) : ExecutionState

    data class Success(val result: String) : ExecutionState
}