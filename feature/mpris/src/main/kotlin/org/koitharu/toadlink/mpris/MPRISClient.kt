package org.koitharu.toadlink.mpris

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.koitharu.toadlink.client.RemoteProcessException
import org.koitharu.toadlink.client.SshConnection
import org.koitharu.toadlink.core.DeviceDescriptor
import kotlin.math.absoluteValue

class MPRISClient(
    private val connection: SshConnection,
) {

    val device: DeviceDescriptor
        get() = connection.host

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

    suspend fun fastForward(seconds: Int = 10) {
        if (seconds < 0) {
            rewind(seconds.absoluteValue)
        } else {
            connection.execute("playerctl position +$seconds")
        }
    }

    suspend fun rewind(seconds: Int = 10) {
        require(seconds > 0) { "Cannot rewind be $seconds seconds" }
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

    fun observeState(): Flow<PlayerState> {
        return connection.executeContinuously("playerctl status -F")
            .map { x -> parseState(x) }
            .onStart { emit(PlayerState.UNKNOWN) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    fun observeMetadata(): Flow<PlayerMetadata?> {
        return connection.executeContinuously("playerctl metadata -f ${PlayerMetadata.FORMAT} -F")
            .map { line -> PlayerMetadata.tryParse(line) }
            .onStart { emit(null) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    private fun parseState(rawValue: String): PlayerState = when (rawValue.lowercase()) {
        "playing" -> PlayerState.PLAYING
        "paused" -> PlayerState.PAUSED
        else -> PlayerState.UNKNOWN
    }
}