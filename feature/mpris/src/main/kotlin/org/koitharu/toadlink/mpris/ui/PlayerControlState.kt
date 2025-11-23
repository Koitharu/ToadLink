package org.koitharu.toadlink.mpris.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import org.koitharu.toadlink.mpris.PlayerMetadata
import org.koitharu.toadlink.mpris.PlayerState

@Immutable
internal data class PlayerControlState(
    val state: PlayerState,
    val metadata: PlayerMetadata?,
    val cover: Bitmap?,
) {

    constructor() : this(
        state = PlayerState.UNKNOWN,
        metadata = null,
        cover = null,
    )
}