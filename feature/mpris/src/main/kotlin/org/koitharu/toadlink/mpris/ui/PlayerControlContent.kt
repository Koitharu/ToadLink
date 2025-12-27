package org.koitharu.toadlink.mpris.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.toadlink.mpris.ui.PlayerControlEffect.OnError
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.composables.EmptyState
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.util.displayMessage
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
    PlayerControlMainContent(
        contentPadding = contentPadding,
        state = state,
        intentHandler = viewModel,
    )
}

@Composable
private fun PlayerControlMainContent(
    contentPadding: PaddingValues,
    state: PlayerControlState,
    intentHandler: MviIntentHandler<PlayerControlAction>,
) = when (state) {
    PlayerControlState.Loading -> Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }

    PlayerControlState.NotPlaying -> EmptyState(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        message = stringResource(R.string.player_not_playing),
        icon = painterResource(R.drawable.ic_music_off),
    )

    PlayerControlState.NotSupported -> EmptyState(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        icon = painterResource(R.drawable.ic_music_off),
        message = stringResource(R.string.player_not_supported),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.player_not_supported_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }

    is PlayerControlState.Player ->
        PlayerControlView(
            modifier = Modifier.padding(contentPadding),
            metadata = state.metadata,
            state = state.state,
            isLoading = false,
            handleAction = intentHandler::handleIntent,
        )

    is PlayerControlState.Error -> EmptyState(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        message = state.error.displayMessage(),
        icon = painterResource(R.drawable.ic_music_off),
    )
}

@Preview
@Composable
private fun PreviewPlayerContent() = MaterialTheme {
    PlayerControlMainContent(
        contentPadding = PaddingValues.Zero,
        state = PlayerControlState.NotSupported,
        intentHandler = MviIntentHandler.NoOp,
    )
}