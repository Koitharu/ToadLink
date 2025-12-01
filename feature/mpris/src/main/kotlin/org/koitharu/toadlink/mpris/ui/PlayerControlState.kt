package org.koitharu.toadlink.mpris.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import org.koitharu.toadlink.mpris.PlayerMetadata
import org.koitharu.toadlink.mpris.PlayerState

@Immutable
internal sealed interface PlayerControlState {

    data object Loading : PlayerControlState

    data object NotPlaying : PlayerControlState

    data object NotSupported : PlayerControlState

    data class Player(
        val state: PlayerState,
        val metadata: PlayerMetadata?,
        val cover: Bitmap?,
    ) : PlayerControlState
}