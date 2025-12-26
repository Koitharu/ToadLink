package org.koitharu.toadlink.mpris.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.mpris.MPRISClient
import org.koitharu.toadlink.mpris.PlayerState
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Next
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Pause
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Play
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.PlayPause
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Prev
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Rewind
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Seek
import org.koitharu.toadlink.mpris.ui.PlayerControlEffect.OnError
import org.koitharu.toadlink.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class PlayerControlViewModel @Inject constructor(
    connectionManager: SshConnectionManager,
) : MviViewModel<PlayerControlState, PlayerControlAction, PlayerControlEffect>(
    initialState = PlayerControlState.Loading
) {

    private var intentJob: Job? = null

    private val client = connectionManager.activeConnection.mapLatest { connection ->
        if (connection == null) {
            null
        } else {
            MPRISClient(connection)
        }
    }.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            client.collectLatest {
                if (it == null) {
                    state.value = PlayerControlState.NotPlaying
                } else {
                    val isSupported = runCatchingCancellable {
                        it.isSupported()
                    }.getOrDefault(true)
                    if (isSupported) {
                        it.observeAll()
                    } else {
                        state.value = PlayerControlState.NotSupported
                    }
                }
            }
        }
    }

    override fun handleIntent(intent: PlayerControlAction) {
        val mpris = client.value ?: return
        val prevJob = intentJob
        intentJob = viewModelScope.launch {
            prevJob?.join()
            runCatchingCancellable {
                when (intent) {
                    Next -> mpris.nextTrack()
                    Pause -> mpris.pause()
                    Play -> mpris.play()
                    PlayPause -> mpris.playPause()
                    Prev -> mpris.previousTrack()
                    is Rewind -> mpris.fastForward(intent.delta)
                    is Seek -> mpris.setPosition(intent.position)
                }
            }.onFailure { error ->
                error.printStackTrace()
                sendEffect(OnError(error))
            }
        }
    }

    private suspend fun MPRISClient.observeAll() = coroutineScope {
        combine(
            observeState(),
            observeMetadata()
        ) { state, metadata ->
            PlayerControlState.Player(
                state = state,
                metadata = metadata,
                cover = metadata?.artUrl?.let { peekCoverArt(it) },
            )
        }.collectLatest {
            state.value = if (it.state == PlayerState.UNKNOWN && it.metadata == null) {
                PlayerControlState.NotPlaying
            } else {
                it
            }
            val coverUrl = it.metadata?.artUrl
            if (it.cover == null && !coverUrl.isNullOrEmpty()) {
                val cover = runCatchingCancellable {
                    getCoverArt(coverUrl)
                }.onFailure { error ->
                    error.printStackTrace()
                }.getOrNull()
                state.value = it.copy(cover = cover)
            }
        }
    }
}