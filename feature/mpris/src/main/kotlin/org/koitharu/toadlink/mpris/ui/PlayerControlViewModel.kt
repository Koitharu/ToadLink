package org.koitharu.toadlink.mpris.ui

import android.graphics.Bitmap
import android.util.LruCache
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
import org.koitharu.toadconnect.client.SshConnectionManager
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.mpris.MPRISClient
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
) : MviViewModel<PlayerControlState, PlayerControlAction, PlayerControlEffect>(PlayerControlState()) {

    private var intentJob: Job? = null
    private var coverCache = object : LruCache<String, Bitmap>(4) {
        override fun entryRemoved(
            evicted: Boolean,
            key: String?,
            oldValue: Bitmap?,
            newValue: Bitmap?,
        ) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            oldValue?.recycle()
        }
    }

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
                    state.value = PlayerControlState()
                } else {
                    it.observeAll()
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
                    is Rewind -> mpris.rewind(intent.delta)
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
            PlayerControlState(
                state = state,
                metadata = metadata,
                cover = metadata?.artUrl?.let { coverCache.get(it) },
            )
        }.collectLatest {
            state.value = it
            val coverUrl = it.metadata?.artUrl
            if (it.cover == null && !coverUrl.isNullOrEmpty()) {
                val cover = runCatchingCancellable {
                    getCoverArt(coverUrl)
                }.onFailure { error ->
                    error.printStackTrace()
                }.getOrNull()
                coverCache.put(coverUrl, cover)
                state.value = it.copy(cover = cover)
            }
        }
    }
}