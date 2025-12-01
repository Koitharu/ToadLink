package org.koitharu.toadlink.mpris

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koitharu.toadconnect.client.RemoteProcessException
import org.koitharu.toadconnect.client.SshConnection

class MPRISClient(
    private val connection: SshConnection,
) {

    suspend fun isSupported(): Boolean = try {
        connection.execute("playerctl --version").startsWith("v")
    } catch (e: RemoteProcessException) {
        if (e.exitCode == RemoteProcessException.EXIT_CODE_NOT_FOUND) {
            false
        } else {
            throw e
        }
    }

    suspend fun play() {
        connection.execute("playerctl play")
    }

    suspend fun pause() {
        connection.execute("playerctl pause")
    }

    suspend fun playPause() {
        connection.execute("playerctl play-pause")
    }

    suspend fun skip(seconds: Int) {
        connection.execute("playerctl position +$seconds")
    }

    suspend fun rewind(seconds: Int) {
        connection.execute("playerctl position -$seconds")
    }

    suspend fun setPosition(seconds: Int) {
        connection.execute("playerctl position $seconds")
    }

    suspend fun previousTrack() {
        connection.execute("playerctl previous")
    }

    suspend fun nextTrack() {
        connection.execute("playerctl next")
    }

    suspend fun isPlaying(): Boolean {
        return connection.execute("playerctl status")
            .equals("Playing", ignoreCase = true)
    }

    suspend fun getPlayerStatus(): PlayerState {
        val rawValue = connection.execute("playerctl status")
        return parseState(rawValue)
    }

    suspend fun getCoverArt(url: String): Bitmap {
        val path = url.removePrefix("file://") // todo more flexible
        val bytes = connection.getFileContent(path)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun observeState(): Flow<PlayerState> {
        return connection.executeContinuously("playerctl status -F")
            .map { x -> parseState(x) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    fun observeMetadata(): Flow<PlayerMetadata?> {
        return connection.executeContinuously("playerctl metadata -f ${PlayerMetadata.FORMAT} -F")
            .map { line -> PlayerMetadata.tryParse(line) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    private fun parseState(rawValue: String): PlayerState = when (rawValue.lowercase()) {
        "playing" -> PlayerState.PLAYING
        "paused" -> PlayerState.PAUSED
        else -> PlayerState.UNKNOWN
    }
}