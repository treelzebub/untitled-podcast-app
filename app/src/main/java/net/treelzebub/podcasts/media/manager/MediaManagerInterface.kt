package net.treelzebub.podcasts.media.manager

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import net.treelzebub.podcasts.ui.models.EpisodeUi

/**
 * Main interface for media management operations
 */
interface MediaManagerInterface {
    
    suspend fun initialize(context: Context, listener: Player.Listener)
    
    /**
     * Prepare and play an episode
     */
    suspend fun prepareAndPlay(episode: EpisodeUi)
    
    /**
     * Add an episode to the queue
     */
    suspend fun addToQueue(episode: EpisodeUi)
    
    /**
     * Toggle play/pause state
     */
    suspend fun playPause()
    
    /**
     * Listen to position updates
     */
    suspend fun listenPosition(speed: Float = 1.0f, block: (Long, Long) -> Unit)
    
    /**
     * Check if an episode is in the queue
     */
    suspend fun isInQueue(episodeId: String): Boolean
    
    /**
     * Get the current playlist
     */
    suspend fun getPlaylist(): List<MediaItem>
    
    /**
     * Get the current episode ID
     */
    suspend fun getCurrentEpisodeId(): String?

    suspend fun getCurrentPosition(): Long

    suspend fun getDuration(): Long
    
    /**
     * Set the current episode ID
     */
    suspend fun setCurrentEpisodeId(episodeId: String)
    
    /**
     * Add a player listener
     */
    suspend fun addListener(listener: Player.Listener)
    
    /**
     * Remove a player listener
     */
    suspend fun removeListener(listener: Player.Listener)
    
    /**
     * Clean up resources
     */
    suspend fun cleanup()
}