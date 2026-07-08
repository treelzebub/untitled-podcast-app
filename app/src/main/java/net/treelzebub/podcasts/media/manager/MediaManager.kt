package net.treelzebub.podcasts.media.manager

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.media.player.PlayerControllerInterface
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.util.mediaItems
import net.treelzebub.podcasts.util.toMediaItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class MediaManager @Inject constructor(
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val playerController: PlayerControllerInterface
) : MediaManagerInterface {
    
    override suspend fun initialize(context: Context, listener: Player.Listener) {
        playerController.initialize(context, listener)
    }
    
    override suspend fun prepareAndPlay(episode: EpisodeUi) {
        val mediaItem = episode.toMediaItem()
        playerController.prepareAndPlay(mediaItem, episode.positionMillis)
    }
    
    override suspend fun addToQueue(episode: EpisodeUi) {
        val mediaItem = episode.toMediaItem()
        playerController.addToPlaylist(mediaItem)
    }
    
    override suspend fun playPause() {
        playerController.playPause()
    }
    
    override suspend fun listenPosition(speed: Float, block: (Long, Long) -> Unit) {
        var shouldContinue = true
        val interval = (speed * 1_000f).toLong()
        
        // Initial offset to prevent janky ticks
        val currentPosition = playerController.getCurrentPosition()
        val offset = (interval - (currentPosition % interval)).coerceAtLeast(0)
        delay(offset)
        
        while (shouldContinue) {
            val position = playerController.getCurrentPosition()
            val duration = playerController.getDuration()
            block(position, duration)
            shouldContinue = playerController.isPlaying()
            delay(interval)
        }
    }
    
    override suspend fun isInQueue(episodeId: String): Boolean {
        return playerController.isInPlaylist(episodeId)
    }
    
    override suspend fun getPlaylist(): List<androidx.media3.common.MediaItem> {
        return playerController.getPlaylist()
    }
    
    override suspend fun getCurrentEpisodeId(): String? {
        return playerController.withPlayer {
            currentMediaItem?.mediaId
        }
    }

    override suspend fun getCurrentPosition(): Long {
        return playerController.getCurrentPosition()
    }

    override suspend fun getDuration(): Long {
        return playerController.getDuration()
    }
    
    override suspend fun addListener(listener: Player.Listener) {
        playerController.addListener(listener)
    }
    
    override suspend fun removeListener(listener: Player.Listener) {
        playerController.removeListener(listener)
    }
    
    override suspend fun cleanup() {
        // Cleanup is handled by the service
    }
}