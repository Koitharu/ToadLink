package org.koitharu.toadlink.mpris

import androidx.annotation.FloatRange
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.koitharu.toadlink.client.exceptions.RemoteProcessException
import org.koitharu.toadlink.client.SshConnection
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.util.logErrors
import org.koitharu.toadlink.core.util.tickerFlow
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

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

    suspend fun play(player: String? = null) {
        connection.execute("playerctl play".withPlayer(player))
    }

    suspend fun pause(player: String? = null) {
        connection.execute("playerctl pause".withPlayer(player))
    }

    suspend fun playPause(player: String? = null) {
        connection.execute("playerctl play-pause".withPlayer(player))
    }

    suspend fun fastForward(seconds: Int = 10, player: String? = null) {
        if (seconds < 0) {
            rewind(seconds.absoluteValue)
        } else {
            connection.execute("playerctl position $seconds+".withPlayer(player))
        }
    }

    suspend fun rewind(seconds: Int = 10, player: String? = null) {
        require(seconds > 0) { "Cannot rewind by $seconds seconds" }
        connection.execute("playerctl position $seconds-".withPlayer(player))
    }

    suspend fun setPosition(seconds: Int, player: String? = null) {
        connection.execute("playerctl position $seconds".withPlayer(player))
    }

    suspend fun previousTrack(player: String? = null) {
        connection.execute("playerctl previous".withPlayer(player))
    }

    suspend fun nextTrack(player: String? = null) {
        connection.execute("playerctl next".withPlayer(player))
    }

    suspend fun isPlaying(player: String? = null): Boolean {
        return connection.execute("playerctl status".withPlayer(player))
            .equals("Playing", ignoreCase = true)
    }

    suspend fun getPlayerStatus(player: String? = null): PlayerState {
        val rawValue = connection.execute("playerctl status".withPlayer(player))
        return parseState(rawValue)
    }

    suspend fun adjustVolume(delta: Float, player: String? = null) {
        val deltaString = when {
            delta < 0 -> "${delta.absoluteValue}-"
            delta > 0 -> "$delta+"
            else -> return
        }
        connection.execute("playerctl volume $deltaString".withPlayer(player))
    }

    suspend fun setVolume(@FloatRange(from = 0.0, to = 1.0) volume: Float, player: String? = null) {
        connection.execute("playerctl volume $volume".withPlayer(player))
    }

    suspend fun getCurrentVolume(player: String? = null): Float {
        return connection.execute("playerctl volume".withPlayer(player)).toFloat()
    }

    suspend fun getPlayersList(): ImmutableList<String> {
        return connection.execute("playerctl --list-all").lines().toImmutableList()
    }

    suspend fun getState(player: String? = null): PlayerState {
        return parseState(connection.execute("playerctl status".withPlayer(player)))
    }

    suspend fun getMetadata(player: String? = null): PlayerMetadata? {
        return PlayerMetadata.tryParse(
            connection.execute(
                "playerctl metadata -f ${PlayerMetadata.FORMAT}"
                    .withPlayer(player)
            )
        )
    }

    fun observeState(player: String? = null): Flow<PlayerState> {
        return connection.executeContinuously("playerctl status -F".withPlayer(player))
            .map { x -> parseState(x) }
            .onStart { emit(PlayerState.UNKNOWN) }
            .distinctUntilChanged()
            .logErrors()
            .flowOn(Dispatchers.Default)
    }

    fun observeMetadata(player: String? = null): Flow<PlayerMetadata?> {
        return connection.executeContinuously(
            "playerctl metadata -f ${PlayerMetadata.FORMAT} -F"
                .withPlayer(player)
        )
            .map { line -> PlayerMetadata.tryParse(line) }
            .onStart { emit(null) }
            .distinctUntilChanged()
            .logErrors()
            .flowOn(Dispatchers.Default)
    }

    fun observeRunningPlayers(): Flow<ImmutableList<String>> {
        return tickerFlow(10.seconds)
            .map { getPlayersList() }
            .distinctUntilChanged()
            .logErrors()
            .flowOn(Dispatchers.Default)
    }

    private fun parseState(rawValue: String): PlayerState = when (rawValue.lowercase()) {
        "playing" -> PlayerState.PLAYING
        "paused" -> PlayerState.PAUSED
        else -> PlayerState.UNKNOWN
    }

    private fun String.withPlayer(player: String?) = player?.let {
        "$this -p $it"
    } ?: this
}