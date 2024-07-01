package net.treelzebub.podcasts.ui.vm

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
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
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.media.PodcastNotificationManager
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.AddToQueue
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Archive
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.PlayPause
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Share
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.ToggleBookmarked
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.ToggleHasPlayed
import timber.log.Timber


@UnstableApi
@HiltViewModel(assistedFactory = EpisodeDetailViewModel.Factory::class)
class EpisodeDetailViewModel @AssistedInject constructor(
    @Assisted episodeId: String,
    private val app: Application,
    private val player: ExoPlayer,
    private val repo: PodcastsRepo,
    private val queueStore: QueueStore,
    private val notificationManagerFactory: PodcastNotificationManager.Factory
) : StatefulViewModel<EpisodeDetailViewModel.State>(State()) {

    companion object {
        const val TIMEOUT = 5000L
        const val SESSION_INTENT_REQUEST_CODE = 0xf00d
    }

    @AssistedFactory
    interface Factory {
        fun create(episodeId: String): EpisodeDetailViewModel
    }

    @Stable
    data class State(
        val loading: Boolean = true,
        val episode: EpisodeUi? = null,
        val queueIndex: Int = 0,
        val bufferedPercentage: Int = 0,
        val durationMillis: Long = 0L,
        val progressMillis: Long = 0,
        val isPlaying: Boolean = false
    )

    enum class EpisodeDetailAction {
        ToggleBookmarked, Share, Download, AddToQueue, PlayPause, ToggleHasPlayed, Archive
    }


    private lateinit var notificationManager: PodcastNotificationManager
    private val mediaSession: MediaSession = MediaSession.Builder(app, player).build()
    private val listener = PodcastPlayerListener()

    private var hasStarted = false

    val actionHandler: (EpisodeDetailAction) -> Unit = { action ->
        Timber.d("Received action: $action")
        when (action) {
            ToggleBookmarked -> toggleBookmarked()
            Share -> share()
            Download -> download()
            AddToQueue -> addToQueue()
            PlayPause -> playPause()
            ToggleHasPlayed -> toggleHasPlayed()
            Archive -> archive()
        }
    }

    init {
        loadEpisode(episodeId)
    }

    private fun loadEpisode(episodeId: String) {
        viewModelScope.launch {
            val episode = repo.getEpisodeById(episodeId)
                ?: throw IllegalArgumentException("Episode is not in database!")
            Timber.d("Got episode with id: ${episode.id}")
            with (episode) {
                _state.update { it.copy(episode = this) }
                queueStore.add(this) {}
            }
            _state.update { it.copy(loading = false) }
        }.invokeOnCompletion { error ->
            error?.let { Timber.e(it) }
        }
        prepareQueue()
    }

    @UnstableApi
    private fun prepareQueue() {
        Timber.d("Preparing Queue")
        viewModelScope.launch {
            queueStore.stateFlow.collect {
                val mediaSourceQueue = it.list.map { item ->
                    val mediaMetaData = MediaMetadata.Builder()
                        .setArtworkUri(Uri.parse(item.imageUrl))
                        .setTitle(item.title)
                        .setAlbumArtist(item.podcastTitle)
                        .build()
                    val trackUri = Uri.parse(item.streamingLink)
                    val mediaItem = MediaItem.Builder()
                        .setUri(trackUri)
                        .setMediaId(item.id)
                        .setMediaMetadata(mediaMetaData)
                        .build()
                    val dataSourceFactory = DefaultDataSource.Factory(app)
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                }
                player.setMediaSources(mediaSourceQueue)
            }
        }

        showNotif(app)
    }

    fun seekTo(position: Long) = player.seekTo(position)

    private fun showNotif(context: Context) {
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
        mediaSession.setSessionActivity(sessionActivityPendingIntent!!)
        notificationManager = notificationManagerFactory.create(
            sessionToken = mediaSession.token,
            listener = PlayerNotificationListener()
        )

        with(player) {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(), true
            )
            addListener(listener)
            playWhenReady = false
            prepare()
        }

        notificationManager.show(player)
    }

    override fun onCleared() {
        if (!hasStarted) return
        hasStarted = false
        mediaSession.run { release() }
        notificationManager.hide()
        player.removeListener(listener)
        // serviceScope.cancel()
        super.onCleared()
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            Timber.d("onNotificationPosted")
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            Timber.d("onNotificationCancelled")
        }
    }

    private fun toggleBookmarked() {
        viewModelScope.launch {
            state.value.episode?.let { repo.setIsBookmarked(it.id, !it.isBookmarked) }
        }
    }

    private fun share() {

    }

    private fun playPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    private fun download() {

    }

    private fun addToQueue() {
        viewModelScope.launch {
            state.value.episode?.let { repo.addToQueue(it) { TODO() } }
        }
    }

    private fun toggleHasPlayed() {
        viewModelScope.launch {
            state.value.episode?.let { repo.setHasPlayed(it.id, !it.hasPlayed) }
        }
    }

    private fun archive() {
        viewModelScope.launch {
            state.value.episode?.let { repo.setIsArchived(it.id, !it.isArchived) }
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

    private inner class PodcastPlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Timber.d("onPlaybackStateChanged: $playbackState")
            updateState()
            when (playbackState) {
                Player.STATE_BUFFERING, Player.STATE_READY -> notificationManager.show(player)
                else -> notificationManager.hide()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Timber.d("onMediaItemTransition: ${mediaItem?.mediaMetadata?.title}; Reason: $reason")
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
}
