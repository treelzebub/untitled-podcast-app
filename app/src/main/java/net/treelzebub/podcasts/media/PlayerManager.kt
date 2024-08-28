package net.treelzebub.podcasts.media

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.service.PlaybackService
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@OptIn(UnstableApi::class)
class PlayerManager @Inject constructor(
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    //private val queueStore: QueueStore
) {

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    suspend fun init(
        @ApplicationContext context: Context, listener: Player.Listener) = withContext(mainDispatcher) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            controllerFuture.get().addListener(listener)
        }, MoreExecutors.directExecutor())
    }

    fun buildPlayer(context: Context, listener: Player.Listener): Player {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true
            )
            .setSeekBackIncrementMs(10_000L)
            .setSeekForwardIncrementMs(5_000L)
            .setHandleAudioBecomingNoisy(true)
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .build()
            .also {
                it.addListener(listener)
            }
    }

    suspend fun <T> withPlayer(block: suspend MediaController.() -> T): T = withContext(mainDispatcher) {
        controllerFuture.get().block()
    }

    /**
     * Update the [block] every second with the latest currentPosition of the player.
     * [block] parameters are:
     *   [Long] the current position in milliseconds, and
     *   [Long] the duration of the episode in milliseconds.
     *
     * [Player]s can only be referenced on the main thread, but we ensure that all delay intervals are invoked
     * in the background. The initial offset prevents janky ticks that would otherwise result from polling the
     * Player's position.
     */
    suspend fun listenPosition(speed: Float = 1.0f, block: (Long, Long) -> Unit) {
        var go = true
        val interval = speed * 1_000f
        val offset = withPlayer {
            interval - (currentPosition % interval)
        }
        delay(offset.toLong())
        while (go) {
            withPlayer {
                block(currentPosition, contentDuration)
                go = isPlaying
            }
            delay(interval.toLong())
        }
    }

    suspend fun prepareIfNeeded(episodeUi: EpisodeUi, listener: Player.Listener) = withPlayer {
        val currentEpisodeId = sessionExtras.getString(PlaybackService.KEY_EPISODE_ID)
        val isSameEpisode = episodeUi.id == currentEpisodeId
        if (playbackState != STATE_IDLE && isSameEpisode) return@withPlayer

        addListener(listener)
        sessionExtras.putString(PlaybackService.KEY_EPISODE_ID, episodeUi.id)
        setMediaItem(episodeUi.toMediaItem(), episodeUi.positionMillis)
        playWhenReady = false
        prepare()
    }

    suspend fun playPause() = withPlayer {
        if (isPlaying) pause() else play()
    }

    suspend fun addListener(listener: Player.Listener) = withPlayer {
        addListener(listener)
    }

    suspend fun removeListener(listener: Player.Listener) = withPlayer {
        removeListener(listener)
    }
}
