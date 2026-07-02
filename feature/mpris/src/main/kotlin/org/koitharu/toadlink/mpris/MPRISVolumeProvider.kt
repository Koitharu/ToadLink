package org.koitharu.toadlink.mpris

import android.media.AudioManager
import android.media.VolumeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koitharu.toadlink.core.util.runCatchingCancellable

class MPRISVolumeProvider(
    private val scope: CoroutineScope,
    private val client: MPRISClient,
) : VolumeProvider(
    VOLUME_CONTROL_ABSOLUTE,
    20,
    10,
) {

    init {
        scope.launch(Dispatchers.Default) {
            runCatchingCancellable {
                client.getCurrentVolume()
            }.onSuccess {
                currentVolume = (it * maxVolume).toInt()
            }
        }
    }

    override fun onAdjustVolume(direction: Int) {
        scope.launch(Dispatchers.Default) {
            runCatchingCancellable {
                val delta = when (direction) {
                    AudioManager.ADJUST_LOWER -> -0.1f
                    AudioManager.ADJUST_RAISE -> 0.1f
                    else -> 0f
                }
                client.adjustVolume(delta)
                client.getCurrentVolume()
            }.onSuccess {
                currentVolume = (it * maxVolume).toInt()
            }
        }
    }

    override fun onSetVolumeTo(volume: Int) {
        scope.launch(Dispatchers.Default) {
            runCatchingCancellable {
                client.setVolume(volume / maxVolume.toFloat())
                client.getCurrentVolume()
            }.onSuccess {
                currentVolume = (it * maxVolume).toInt()
            }
        }
    }
}