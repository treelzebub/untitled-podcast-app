package net.treelzebub.podcasts.media.manager

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import net.treelzebub.podcasts.ui.models.EpisodeUi

interface MediaManagerInterface {

    suspend fun initialize(context: Context, listener: Player.Listener)

    suspend fun prepareAndPlay(episode: EpisodeUi)

    suspend fun addToQueue(episode: EpisodeUi)

    suspend fun playPause()

    suspend fun listenPosition(speed: Float = 1.0f, block: (Long, Long) -> Unit)

    suspend fun isInQueue(episodeId: String): Boolean

    suspend fun getPlaylist(): List<MediaItem>

    suspend fun getCurrentEpisodeId(): String?

    suspend fun getCurrentPosition(): Long

    suspend fun getDuration(): Long

    suspend fun setCurrentEpisodeId(episodeId: String)

    suspend fun addListener(listener: Player.Listener)

    suspend fun removeListener(listener: Player.Listener)

    suspend fun cleanup()
}
