package org.koitharu.toadlink.mpris

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import androidx.core.app.PendingIntentCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koitharu.toadlink.core.util.runCatchingCancellable
import java.util.concurrent.TimeUnit


class MediaSessionHelper(
    private val context: Context,
    private val client: MPRISClient,
) : MediaSession.Callback(), AutoCloseable {

    private val coroutineScope = MainScope()
    private val metadataBuilder = MediaMetadata.Builder()
    private val stateBuilder = PlaybackState.Builder()
        .setActions(
            PlaybackState.ACTION_PLAY or
                    PlaybackState.ACTION_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or
                    PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackState.ACTION_SEEK_TO or
                    PlaybackState.ACTION_FAST_FORWARD or
                    PlaybackState.ACTION_REWIND
        )
    private val mediaSession = MediaSession(context, SESSION_TAG)

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
//        val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_NONE)
//            .build()
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS)
        mediaSession.setCallback(this)
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            mediaSession.setSessionActivity(
                PendingIntentCompat.getActivity(
                    context.applicationContext,
                    0,
                    intent,
                    0,
                    false
                )
            )
        }
    }

    override fun close() {
        coroutineScope.cancel()
        mediaSession.release()
    }

    suspend fun getMediaSession(metadata: PlayerMetadata, state: PlayerState): MediaSession {
        val coverArt = metadata.artUrl?.let { coverUrl ->
            runCatchingCancellable {
                client.getCoverArt(coverUrl)
            }.getOrNull()
        }
        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, coverArt);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, metadata.title)
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, metadata.album)
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, metadata.artist)
        metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, metadata.length * 1000L)
        stateBuilder.setState(
            when (state) {
                PlayerState.PLAYING -> PlaybackState.STATE_PLAYING
                PlayerState.PAUSED -> PlaybackState.STATE_PAUSED
                PlayerState.UNKNOWN -> PlaybackState.STATE_NONE
            },
            TimeUnit.SECONDS.toMillis(metadata.position.toLong()),
            1f
        )
        mediaSession.isActive = state == PlayerState.PLAYING
        mediaSession.setMetadata(metadataBuilder.build());
        mediaSession.setPlaybackState(stateBuilder.build())
        return mediaSession
    }

    override fun onPause() = runCommand {
        client.pause()
    }

    override fun onPlay() = runCommand {
        client.play()
    }

    override fun onFastForward() = runCommand {
        client.fastForward()
    }

    override fun onRewind() = runCommand {
        client.rewind()
    }

    override fun onSeekTo(pos: Long) = runCommand {
        client.setPosition(TimeUnit.MILLISECONDS.toSeconds(pos).toInt())
    }

    override fun onSkipToNext() = runCommand {
        client.nextTrack()
    }

    override fun onSkipToPrevious() = runCommand {
        client.previousTrack()
    }

    private inline fun runCommand(crossinline block: suspend MPRISClient.() -> Unit) {
        coroutineScope.launch {
            runCatching { client.block() }
        }
    }

    private companion object {

        const val SESSION_TAG = "mpris_client"
    }
}