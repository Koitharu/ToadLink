package org.koitharu.toadlink.mpris.ui

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.DeviceDescriptor
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
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.SelectPlayer
import org.koitharu.toadlink.mpris.ui.PlayerControlEffect.OnError
import org.koitharu.toadlink.ui.mvi.MviViewModel

@HiltViewModel(assistedFactory = PlayerControlViewModel.Factory::class)
internal class PlayerControlViewModel @AssistedInject constructor(
    @Assisted host: DeviceDescriptor,
    connectionManager: SshConnectionManager,
) : MviViewModel<PlayerControlState, PlayerControlAction, PlayerControlEffect>(
    initialState = PlayerControlState.Loading
) {

    private var intentJob: Job? = null
    private val isProcessing = MutableStateFlow(false)
    private val currentPlayer = MutableStateFlow<String?>(null)

    private val client = connectionManager.observeConnection(host).mapLatest { connection ->
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
        intentJob = viewModelScope.launch(Dispatchers.Default) {
            prevJob?.join()
            isProcessing.value = true
            val player = currentPlayer.value
            try {
                when (intent) {
                    Next -> mpris.nextTrack(player)
                    Pause -> mpris.pause(player)
                    Play -> mpris.play(player)
                    PlayPause -> mpris.playPause(player)
                    Prev -> mpris.previousTrack(player)
                    is Rewind -> mpris.fastForward(intent.delta, player)
                    is Seek -> mpris.setPosition(intent.position, player)
                    is SelectPlayer -> currentPlayer.value = intent.player
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                sendEffect(OnError(e))
            } finally {
                isProcessing.value = false
            }
        }
    }

    private suspend fun MPRISClient.observeAll() {
        currentPlayer.flatMapLatest { player ->
            observePlayerState(player)
        }.catch { error ->
            emit(PlayerControlState.Error(error))
        }.collectLatest { newState ->
            state.value = newState
        }
    }

    private fun MPRISClient.observePlayerState(player: String?) = combine(
        observeState(player),
        observeMetadata(player),
        observeRunningPlayers(),
        isProcessing,
    ) { state, metadata, players, processing ->
        if (state == PlayerState.UNKNOWN && metadata == null) {
            PlayerControlState.NotPlaying
        } else {
            PlayerControlState.Player(
                state = state,
                metadata = metadata,
                isLoading = processing,
                players = players,
                selectedPlayer = player ?: metadata?.playerName,
            )
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(host: DeviceDescriptor): PlayerControlViewModel
    }
}