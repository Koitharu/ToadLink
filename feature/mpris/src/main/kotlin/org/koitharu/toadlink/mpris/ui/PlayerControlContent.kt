package org.koitharu.toadlink.mpris.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.toadlink.mpris.ui.PlayerControlEffect.OnError
import org.koitharu.toadlink.ui.util.getDisplayMessage

@Composable
fun PlayerControlContent(
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = hiltViewModel<PlayerControlViewModel>()
    val state by viewModel.collectState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is OnError -> snackbarHostState.showSnackbar(
                    effect.error.getDisplayMessage(context)
                )
            }
        }.launchIn(this)
    }
    PlayerControlView(
        modifier = Modifier.padding(contentPadding),
        metadata = state.metadata,
        state = state.state,
        coverArt = state.cover,
        isLoading = false,
        handleAction = viewModel::handleIntent
    )
}