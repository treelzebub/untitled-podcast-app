package net.treelzebub.podcasts.media.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

/**
 * Interface for controlling media playback through MediaController
 */
interface PlayerControllerInterface {
    
    /**
     * Initialize the controller
     */
    suspend fun initialize(listener: Player.Listener)
    
    /**
     * Prepare and play a media item
     */
    suspend fun prepareAndPlay(mediaItem: MediaItem, positionMs: Long = 0L)
    
    /**
     * Toggle play/pause state
     */
    suspend fun playPause()
    
    /**
     * Add a media item to the playlist
     */
    suspend fun addToPlaylist(mediaItem: MediaItem)
    
    /**
     * Check if media item is in playlist
     */
    suspend fun isInPlaylist(mediaItemId: String): Boolean
    
    /**
     * Get current playlist
     */
    suspend fun getPlaylist(): List<MediaItem>
    
    /**
     * Get current playback position
     */
    suspend fun getCurrentPosition(): Long
    
    /**
     * Get current media duration
     */
    suspend fun getDuration(): Long
    
    /**
     * Check if player is playing
     */
    suspend fun isPlaying(): Boolean
    
    /**
     * Get current playback state
     */
    suspend fun getPlaybackState(): Int
    
    /**
     * Add a player listener
     */
    suspend fun addListener(listener: Player.Listener)
    
    /**
     * Remove a player listener
     */
    suspend fun removeListener(listener: Player.Listener)
    
    /**
     * Execute a block with the player
     */
    suspend fun <T> withPlayer(block: suspend Player.() -> T): T
}