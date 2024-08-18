package net.treelzebub.podcasts.media

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.di.DefaultDispatcher
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.service.PlaybackService
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@OptIn(UnstableApi::class)
class PlayerManager @Inject constructor(
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
    //private val queueStore: QueueStore
) {

    private val scope = CoroutineScope(mainDispatcher)
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    suspend fun init(@ApplicationContext context: Context, listener: Player.Listener) = onMain {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        withPlayer {
            addListener(listener)
        }
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

    suspend fun listenPosition(block: CoroutineScope.(Long, Long) -> Unit) {
        val player = controllerFuture.get()
        if (player.isPlaying) {
            withContext(defaultDispatcher) {
                val interval = 1000L
                val offset = withContext(mainDispatcher) {
                    interval - (player.currentPosition % interval)
                }
                delay(offset)

                while (true) {
                    val pair = withContext(mainDispatcher) {
                        val currentPosition = player.currentPosition
                        val duration = player.contentDuration
                        currentPosition to duration
                    }
                    block(pair.first, pair.second)
                    delay(interval)
                }
            }
        }
    }

    suspend fun prepare(episodeUi: EpisodeUi, listener: Player.Listener) = withPlayer {
        sessionExtras.putString(PlaybackService.KEY_EPISODE_ID, episodeUi.id)
        addListener(listener)
        setMediaItems(listOf(episodeUi.toMediaItem()), 0, episodeUi.positionMillis)
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

    private suspend fun <T> onMain(block: suspend CoroutineScope.() -> T) = withContext(mainDispatcher, block)
    private suspend fun withPlayer(block: MediaController.() -> Unit) = onMain {
        controllerFuture.addListener({
            if (controllerFuture.isDone) controllerFuture.get().block()
        }, MoreExecutors.directExecutor())
    }
}
