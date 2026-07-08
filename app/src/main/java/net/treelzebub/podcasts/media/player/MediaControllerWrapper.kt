package net.treelzebub.podcasts.media.player

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.service.PlaybackService
import net.treelzebub.podcasts.util.indexOf
import net.treelzebub.podcasts.util.mediaItems
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaController wrapper that follows the proper Media3 pattern
 */
@Singleton
@OptIn(UnstableApi::class)
class MediaControllerWrapper @Inject constructor(
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlayerControllerInterface {
    
    private lateinit var controller: MediaController
    private var currentEpisodeId: String? = null
    
    override suspend fun initialize(listener: Player.Listener) {
        // This will be called with context from the ViewModel
        // The actual initialization happens in prepareAndPlay
    }
    
    override suspend fun prepareAndPlay(mediaItem: MediaItem, positionMs: Long) = onPlayer {
        val shouldPrepare = currentEpisodeId != mediaItem.mediaId ||
                          !isPlaying && playbackState == STATE_IDLE

        if (shouldPrepare) {
            setMediaItem(mediaItem, positionMs)
            prepare()
            currentEpisodeId = mediaItem.mediaId
        }

        play()
    }

    override suspend fun playPause() = onPlayer {
        if (isPlaying) pause() else play()
    }

    override suspend fun addToPlaylist(mediaItem: MediaItem) {
        ensureControllerInitialized()
        if (!isInPlaylist(mediaItem.mediaId)) {
            onPlayer { addMediaItem(mediaItem) }
        }
    }

    override suspend fun isInPlaylist(mediaItemId: String): Boolean {
        return getPlaylist().indexOf(mediaItemId) >= 0
    }

    override suspend fun getPlaylist(): List<MediaItem> {
        return try {
            onPlayer { mediaItems }
        } catch (e: Exception) {
            Timber.e(e, "Error getting playlist")
            emptyList()
        }
    }

    override suspend fun getCurrentPosition(): Long = onPlayer { currentPosition }

    override suspend fun getDuration(): Long = onPlayer { contentDuration }

    override suspend fun isPlaying(): Boolean = onPlayer { isPlaying }

    override suspend fun getPlaybackState(): Int = onPlayer { playbackState }

    override suspend fun addListener(listener: Player.Listener) = onPlayer { addListener(listener) }

    override suspend fun removeListener(listener: Player.Listener) = onPlayer { removeListener(listener) }

    override suspend fun <T> withPlayer(block: suspend Player.() -> T): T {
        ensureControllerInitialized()
        return withContext(mainDispatcher) { controller.block() }
    }

    /**
     * MediaController, like ExoPlayer, throws if touched off the thread it was built on, so
     * every controller access must funnel through here instead of calling `controller` directly.
     */
    private suspend fun <T> onPlayer(block: Player.() -> T): T {
        ensureControllerInitialized()
        return withContext(mainDispatcher) { controller.block() }
    }

    private suspend fun ensureControllerInitialized() {
        if (!::controller.isInitialized) {
            throw IllegalStateException("Controller not initialized. Call initController first.")
        }
    }
    
    /**
     * Initialize the controller with context (called from ViewModel)
     */
    suspend fun initController(@ApplicationContext context: Context, listener: Player.Listener) {
        if (::controller.isInitialized) return
        
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = withContext(mainDispatcher) {
            MediaController.Builder(context, sessionToken).buildAsync()
        }
        
        controller = withContext(ioDispatcher) {
            async { future.get() }.await()
        }

        withContext(mainDispatcher) {
            controller.addListener(listener)
        }
    }
}