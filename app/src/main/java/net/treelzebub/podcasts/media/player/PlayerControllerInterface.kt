package net.treelzebub.podcasts.media.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

interface PlayerControllerInterface {

    suspend fun initialize(listener: Player.Listener)

    suspend fun prepareAndPlay(mediaItem: MediaItem, positionMs: Long = 0L)

    suspend fun playPause()

    suspend fun addToPlaylist(mediaItem: MediaItem)

    suspend fun isInPlaylist(mediaItemId: String): Boolean

    suspend fun getPlaylist(): List<MediaItem>

    suspend fun getCurrentPosition(): Long

    suspend fun getDuration(): Long

    suspend fun isPlaying(): Boolean

    suspend fun getPlaybackState(): Int

    suspend fun addListener(listener: Player.Listener)

    suspend fun removeListener(listener: Player.Listener)

    suspend fun <T> withPlayer(block: suspend Player.() -> T): T
}
