package net.treelzebub.podcasts.ui.components

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.ui.models.EpisodeUi
import timber.log.Timber
import javax.inject.Inject


@androidx.annotation.OptIn(UnstableApi::class)
class PodcastNotificationManager(
    private val context: Context,
    sessionToken: SessionToken,
    private val player: Player,
    notificationListener: PlayerNotificationManager.NotificationListener,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        const val NOTIFICATION_LARGE_ICON_PX = 144 // px
        const val NOW_PLAYING_CHANNEL_ID = "media.NOW_PLAYING"
        const val NOW_PLAYING_NOTIFICATION_ID = 0xb339
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(mainDispatcher + serviceJob)
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaController.Builder(context, sessionToken).buildAsync()
        notificationManager =
            PlayerNotificationManager.Builder(context, NOW_PLAYING_NOTIFICATION_ID, NOW_PLAYING_CHANNEL_ID)
                .setChannelNameResourceId(R.string.notif_channel_name)
                .setChannelDescriptionResourceId(R.string.notif_channel_description)
                .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
                .setNotificationListener(notificationListener)
                .setSmallIconResourceId(R.drawable.ic_launcher_foreground)
                .build()
                .apply {
                    setPlayer(player)
                    setUseRewindAction(true)
                    setUseFastForwardAction(true)
                    setUseRewindActionInCompactView(true)
                    setUseFastForwardActionInCompactView(true)
                    setUseRewindActionInCompactView(true)
                    setUseFastForwardActionInCompactView(true)
                }
    }

    fun hideNotification() = notificationManager.setPlayer(null)

    fun showNotificationForPlayer(player: Player) = notificationManager.setPlayer(player)

    private inner class DescriptionAdapter(
        private val controller: ListenableFuture<MediaController>
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        // TODO replace all this shit with immutable state
        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent? = controller.get().sessionActivity

        override fun getCurrentContentText(player: Player) = controller.get().mediaMetadata.description

        override fun getCurrentContentTitle(player: Player) = controller.get().mediaMetadata.title.toString()

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri = controller.get().mediaMetadata.artworkUri
            return if (currentIconUri != iconUri || currentBitmap == null) {
                // Cache current track's bitmap so that successive calls to
                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = iconUri?.let { resolveUriAsBitmap(it) }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else currentBitmap
        }

        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap {
            return withContext(ioDispatcher) {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .build()
                val result = (loader.execute(request) as SuccessResult).drawable
                (result as BitmapDrawable).bitmap
            }
        }
    }
}

enum class PlayerButton {
    PlayPause, Stop, FastForward, Rewind,
    Previous, Next
}

sealed interface NowPlayingState {
    data class Tracks(val items: List<EpisodeUi>) : NowPlayingState
    data object Loading : NowPlayingState
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val player: ExoPlayer,
    private val repo: PodcastsRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    companion object {
        const val SESSION_INTENT_REQUEST_CODE = 0x1337
    }

    private val _currentPlayingIndex = MutableStateFlow(0)
    val currentPlayingIndex = _currentPlayingIndex.asStateFlow()

    private val _totalDurationInMS = MutableStateFlow(0L)
    val totalDurationInMS = _totalDurationInMS.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    val uiState: StateFlow<NowPlayingState> =
        MutableStateFlow(NowPlayingState.Tracks(emptyList())).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialValue = NowPlayingState.Loading
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
        notificationManager =
            PodcastNotificationManager(
                context,
                mediaSession.token,
                player,
                PlayerNotificationListener(),
                ioDispatcher,
                mainDispatcher
            )
        notificationManager.showNotificationForPlayer(player)
    }

    fun onDestroy() {
        onClose()
        player.release()
    }

    fun onClose() {
        if (!hasStarted) return
        hasStarted = false
        mediaSession.run { release() }
        notificationManager.hideNotification()
        player.removeListener(playerListener)
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
            super.onPlaybackStateChanged(playbackState)
            syncPlayerFlows()
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> notificationManager.showNotificationForPlayer(player)
                else -> notificationManager.hideNotification()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Timber.d("onMediaItemTransition: ${mediaItem?.mediaMetadata?.title}")
            super.onMediaItemTransition(mediaItem, reason)
            syncPlayerFlows()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Timber.d("onIsPlayingChanged: $isPlaying")
            super.onIsPlayingChanged(isPlaying)
            _isPlaying.value = isPlaying
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Timber.e("Error: ${error.message}")
        }
    }

    private fun syncPlayerFlows() {
        _currentPlayingIndex.value = player.currentMediaItemIndex
        _totalDurationInMS.value = player.duration.coerceAtLeast(0L)
    }
}

@Composable
fun NowPlaying() {

}
