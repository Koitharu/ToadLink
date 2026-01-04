package org.koitharu.toadlink.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class MviViewModel<S, I, E>(
    initialState: S,
) : ViewModel(), MviIntentHandler<I> {

    protected val state = MutableStateFlow<S>(initialState)

    private val mutableEffect = MutableSharedFlow<E>(extraBufferCapacity = 1)
    val effect get() = mutableEffect.asSharedFlow()

    protected suspend fun sendEffect(e: E) {
        mutableEffect.emit(e)
    }

    protected fun sendEffectAsync(e: E) = viewModelScope.launch {
        sendEffect(e)
    }

    @Composable
    fun collectState() = state.collectAsState()
}