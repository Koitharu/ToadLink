package org.koitharu.toadlink.mpris.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koitharu.toadlink.core.util.formatTimeSeconds
import org.koitharu.toadlink.mpris.PlayerMetadata
import org.koitharu.toadlink.mpris.PlayerState
import org.koitharu.toadlink.mpris.R
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Next
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Prev
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Rewind
import org.koitharu.toadlink.mpris.ui.PlayerControlAction.Seek
import kotlin.math.roundToInt

@Composable
fun PlayerControlView(
    modifier: Modifier = Modifier,
    metadata: PlayerMetadata?,
    state: PlayerState,
    isLoading: Boolean,
    handleAction: (PlayerControlAction) -> Unit,
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .then(modifier),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val coverPlaceholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainer)
    AsyncImage(
        modifier = Modifier
            .size(260.dp)
            .padding(bottom = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .aspectRatio(1f),
        model = metadata?.artUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        placeholder = coverPlaceholder,
        error = coverPlaceholder,
        fallback = coverPlaceholder,
    )
    Text(
        text = metadata?.title.orEmpty(),
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = metadata?.artist.orEmpty(),
        modifier = Modifier
            .padding(top = 2.dp),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = metadata?.album.orEmpty(),
        modifier = Modifier
            .padding(top = 2.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    PositionSlider(
        length = metadata?.length ?: 0,
        position = metadata?.position ?: 0,
        handleAction = handleAction,
    )
    ButtonBar(
        modifier = Modifier.padding(all = 16.dp),
        handleAction = handleAction,
        isLoading = isLoading,
        state = state,
    )
}

@Composable
private fun PositionSlider(
    length: Int,
    position: Int,
    handleAction: (PlayerControlAction) -> Unit,
) {
    if (length > 0) {
        var targetPosition by remember(position) { mutableIntStateOf(position) }
        Slider(
            value = targetPosition.toFloat(),
            onValueChange = { targetPosition = it.roundToInt() },
            onValueChangeFinished = { handleAction(Seek(targetPosition)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp, top = 12.dp),
            valueRange = 0f..length.toFloat(),
            steps = 0,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 16.dp, start = 16.dp, top = 4.dp)
        ) {
            Text(position.formatTimeSeconds())
            Spacer(modifier = Modifier.weight(1f))
            Text(length.formatTimeSeconds())
        }
    }
}

@Composable
private fun ButtonBar(
    modifier: Modifier = Modifier,
    handleAction: (PlayerControlAction) -> Unit,
    isLoading: Boolean,
    state: PlayerState,
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceAround,
    verticalAlignment = Alignment.CenterVertically,
) {
    IconButton(
        onClick = { handleAction(Prev) },
    ) {
        Icon(painterResource(R.drawable.ic_skip_previous), "previous")
    }
    IconButton(
        onClick = { handleAction(Rewind(-10)) },
    ) {
        Icon(painterResource(R.drawable.ic_fast_rewind), "skip_10")
    }
    Spacer(modifier = Modifier.width(16.dp))
    FilledTonalIconButton(
        modifier = Modifier.size(64.dp),
        enabled = !isLoading,
        onClick = {
            handleAction(
                when (state) {
                    PlayerState.PLAYING -> PlayerControlAction.Pause
                    PlayerState.PAUSED -> PlayerControlAction.Play
                    PlayerState.UNKNOWN -> PlayerControlAction.PlayPause
                }
            )
        },
    ) {
        AnimatedContent(
            targetState = state.takeUnless { isLoading },
            transitionSpec = {
                scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
            }
        ) { targetState ->
            when (targetState) {
                PlayerState.PLAYING -> Icon(painterResource(R.drawable.ic_pause), "pause")
                PlayerState.PAUSED -> Icon(painterResource(R.drawable.ic_play), "play")
                PlayerState.UNKNOWN -> Icon(
                    painterResource(R.drawable.ic_play_pause),
                    "play/pause"
                )

                null -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LocalContentColor.current,
                    strokeWidth = 3.dp,
                )
            }
        }
    }
    Spacer(modifier = Modifier.width(16.dp))
    IconButton(
        onClick = { handleAction(Rewind(10)) },
    ) {
        Icon(painterResource(R.drawable.ic_fast_forward), "skip_10")
    }
    IconButton(
        onClick = { handleAction(Next) },
    ) {
        Icon(painterResource(R.drawable.ic_skip_next), "next")
    }
}

@Composable
@Preview
private fun PreviewPlayerControlView() = PlayerControlView(
    metadata = PlayerMetadata(
        playerName = "Amberol",
        title = "Angel",
        artist = "Rammstein",
        album = "Unknown",
        position = 10,
        length = 124,
        artUrl = null
    ),
    state = PlayerState.PAUSED,
    isLoading = false,
    handleAction = {}
)

@Composable
@Preview
private fun PreviewPlayerControlViewLoading() = PlayerControlView(
    metadata = PlayerMetadata(
        playerName = "Amberol",
        title = "Angel",
        artist = "Rammstein",
        album = "Unknown",
        position = 10,
        length = 124,
        artUrl = null
    ),
    state = PlayerState.PAUSED,
    isLoading = true,
    handleAction = {}
)