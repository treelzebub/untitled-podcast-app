package net.treelzebub.podcasts.ui.components

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.media.PodcastNotificationManager
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.vm.StatefulViewModel
import timber.log.Timber
import javax.inject.Inject


enum class PlayerButton {
    PlayPause, Stop, FastForward, Rewind, Previous, Next
}

sealed interface NowPlayingState {
    data class Tracks(val items: List<EpisodeUi>) : NowPlayingState
    data object Loading : NowPlayingState
}

@UnstableApi
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val player: ExoPlayer,
    private val repo: PodcastsRepo,
    private val queue: QueueStore,
    private val notificationManagerFactory: PodcastNotificationManager.Factory,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : StatefulViewModel<NowPlayingViewModel.State>(State()) {

    companion object {
        const val SESSION_INTENT_REQUEST_CODE = 0xf00d
    }

    data class State(
        val episode: EpisodeUi? = null,
        val queueIndex: Int = 0,
        val bufferedPercentage: Int = 0,
        val durationMillis: Long = 0L,
        val progressMillis: Long = 0,
        val isPlaying: Boolean = false
    )

    private lateinit var notificationManager: PodcastNotificationManager
    private lateinit var mediaSession: MediaSession
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(mainDispatcher + serviceJob)

    private var hasStarted = false

    fun preparePlayer(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player.setAudioAttributes(audioAttributes, true)
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(playerListener)
        setupPlaylist(context)
    }

    @UnstableApi
    private fun setupPlaylist(context: Context) {
        val mediaSourceQueue = listOf<EpisodeUi>().map {
            val mediaMetaData = MediaMetadata.Builder()
                .setArtworkUri(Uri.parse(it.imageUrl))
                .setTitle(it.title)
                .setAlbumArtist(it.podcastTitle)
                .build()
            val trackUri = Uri.parse(it.streamingLink)
            val mediaItem = MediaItem.Builder()
                .setUri(trackUri)
                .setMediaId(it.id)
                .setMediaMetadata(mediaMetaData)
                .build()
            val dataSourceFactory = DefaultDataSource.Factory(context)
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }

        onStart(context)
        player.playWhenReady = false
        player.setMediaSources(mediaSourceQueue)
        player.prepare()
    }

    fun press(button: PlayerButton) {
        when (button) {
            PlayerButton.PlayPause -> if (player.isPlaying) player.pause() else player.play()
            PlayerButton.Stop -> player.stop()
            PlayerButton.FastForward -> player.seekForward()
            PlayerButton.Rewind -> player.seekBack()
            PlayerButton.Previous -> player.seekToPrevious() // see getMaxSeekToPreviousPosition()
            PlayerButton.Next -> player.seekToNextMediaItem()
        }
    }

    fun seekTo(position: Long) = player.seekTo(position)

    fun onStart(context: Context) {
        if (hasStarted) return
        hasStarted = true

        val sessionActivityPendingIntent =
            context.packageManager?.getLaunchIntentForPackage(context.packageName)
                ?.let { sessionIntent ->
                    PendingIntent.getActivity(
                        context,
                        SESSION_INTENT_REQUEST_CODE,
                        sessionIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
        mediaSession = MediaSession.Builder(context, player)
            .setSessionActivity(sessionActivityPendingIntent!!)
            .build()
        notificationManager = notificationManagerFactory.create(
            sessionToken = mediaSession.token,
            listener = PlayerNotificationListener()
        )
        notificationManager.show(player)
    }

    fun onDestroy() {
        onClose()
        player.release()
    }

    fun onClose() {
        if (!hasStarted) return
        hasStarted = false
        mediaSession.run { release() }
        notificationManager.hide()
        player.removeListener(playerListener)
        // serviceScope.cancel()
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            TODO("Not yet implemented.")
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            TODO("Not yet implemented.")
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Timber.d("onPlaybackStateChanged: $playbackState")
            updateState()
            when (playbackState) {
                Player.STATE_BUFFERING, Player.STATE_READY -> notificationManager.show(player)
                else -> notificationManager.hide()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Timber.d("onMediaItemTransition: ${mediaItem?.mediaMetadata?.title}")
            updateState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Timber.d("onIsPlayingChanged: $isPlaying")
            _state.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Timber.e("Error: ${error.message}")
            TODO()
        }
    }

    private fun updateState() {
        _state.update {
            it.copy(
                queueIndex = player.currentMediaItemIndex,
                durationMillis = player.duration.coerceAtLeast(0L),
                bufferedPercentage = player.bufferedPercentage,
                progressMillis = player.currentPosition
            )
        }
    }
}
