package net.treelzebub.podcasts.ui.vm

import android.app.Application
import android.app.Notification
import android.content.ComponentName
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.service.PlaybackService
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.AddToQueue
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.Archive
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.PlayPause
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.Share
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.ToggleBookmarked
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.ToggleHasPlayed
import timber.log.Timber


enum class EpisodeDetailAction {
    ToggleBookmarked, Share, Download, AddToQueue, PlayPause, ToggleHasPlayed, Archive
}

@UnstableApi
@HiltViewModel(assistedFactory = EpisodeDetailViewModel.Factory::class)
class EpisodeDetailViewModel @AssistedInject constructor(
    @Assisted episodeId: String,
    app: Application,
    private val repo: PodcastsRepo,
    private val queueStore: QueueStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : AndroidViewModel(app) {

    @AssistedFactory
    interface Factory {
        fun create(episodeId: String): EpisodeDetailViewModel
    }

    @Stable
    @Immutable
    data class EpisodeState(
        val id: String? = null,
        val imageUrl: String? = null,
        val displayDate: String? = null,
        val duration: String? = null,
        val description: String? = null,
        val streamingLink: String? = null
    ) {
        val isPopulated: Boolean
            // Bare minimum for playback.
            get() = id != null && streamingLink != null
    }

    @Stable
    data class UiState(
        val loading: Boolean = true,
        val queueIndex: Int = 0,
        val bufferedPercentage: Int = 0,
        val durationMillis: Long = 0L,
        val progressMillis: Long = 0L,
        val isPlaying: Boolean = false,
        val hasPlayed: Boolean = false,
        val isBookmarked: Boolean = false,
        val isArchived: Boolean = false
    )


    private val _uiState = MutableStateFlow(UiState())
    private val _episodeState = MutableStateFlow(EpisodeState())
    private val episodeHolder = MutableStateFlow<EpisodeUi?>(null)
    private val listener = PodcastPlayerListener()

    val uiState = _uiState.asStateFlow()
    val episodeState = _episodeState.asStateFlow()

    val actionHandler: (EpisodeDetailAction) -> Unit = { action ->
        Timber.d("Received action: $action")
        when (action) {
            ToggleBookmarked -> toggleBookmarked()
            Share -> share()
            Download -> download()
            AddToQueue -> addToQueue()
            PlayPause -> playPause()
            ToggleHasPlayed -> toggleHasPlayed()
            Archive -> toggleArchived()
        }
    }

    private val sessionToken =
        SessionToken(getApplication(), ComponentName(getApplication(), PlaybackService::class.java))
    private val controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
    private val controller: MediaController?
        get() = controllerFuture.let { if (it.isDone) it.get() else null }

    init {
        loadEpisode(episodeId)
    }

    private fun loadEpisode(episodeId: String) {
        viewModelScope.launch(ioDispatcher) {
            val episode = repo.getEpisodeById(episodeId)
                ?: throw IllegalArgumentException("Episode is not in database!")
            with(episode) {
                episodeHolder.update { this }
                _episodeState.update {
                    it.copy(
                        id = id,
                        imageUrl = imageUrl,
                        displayDate = displayDate,
                        duration = duration,
                        description = description,
                        streamingLink = streamingLink
                    )
                }
                prepareQueue(this)
                _uiState.update {
                    it.copy(
                        loading = false,
                        queueIndex = queueStore.indexFor(id),
                        // durationMillis = ,
                        progressMillis = progressMillis,
                        isBookmarked = isBookmarked,
                        isArchived = isArchived
                    )
                }
            }
        }.invokeOnCompletion { error ->
            error?.let { Timber.e(it) }
        }
    }

    @UnstableApi
    private fun prepareQueue(episode: EpisodeUi) {
        Timber.d("Preparing Queue")
        viewModelScope.launch(ioDispatcher) {
            queueStore.add(episode) { Timber.e("Error preparing queue, adding to queue") }
            queueStore.stateFlow.collect { queue ->
                val mediaItems = queue.asMediaItems()
                withContext(mainDispatcher) {
                    controller?.let {
                        it.setMediaItems(mediaItems)
                        it.addListener(listener)
                        it.playWhenReady = true
                        it.prepare()
                        Timber.d("controller is not null")
                    } ?: Timber.d("controller is null.")
                }
            }
        }
    }

    fun seekTo(position: Long) = controller?.seekTo(position)

    override fun onCleared() {
        controller?.removeListener(listener)
        super.onCleared()
    }

    private fun toggleBookmarked() {
        viewModelScope.launch(ioDispatcher) {
            episodeHolder.value?.let { repo.setIsBookmarked(it.id, !it.isBookmarked) }
            _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }
        }
    }

    private fun share() {
        Timber.d("TODO: Share")
    }

    private fun playPause() {
        controller?.let { player ->
            if (player.isPlaying) player.pause() else player.play()
            _uiState.update { state -> state.copy(isPlaying = player.isPlaying) }
        } ?: return
    }

    private fun download() {
        Timber.d("TODO: Download")
    }

    private fun addToQueue() {
        viewModelScope.launch(ioDispatcher) {
            // TODO UI State -> isInQueue
            episodeHolder.value?.let { repo.addToQueue(it) { TODO() } }
        }
    }

    private fun toggleHasPlayed() {
        viewModelScope.launch(ioDispatcher) {
            episodeHolder.value?.let { repo.setHasPlayed(it.id, !it.hasPlayed) }
            _uiState.update { it.copy(hasPlayed = !it.hasPlayed) }
        }
    }

    private fun toggleArchived() {
        viewModelScope.launch(ioDispatcher) {
            episodeHolder.value?.let { repo.setIsArchived(it.id, !it.isArchived) }
            _uiState.update { it.copy(isArchived = !it.isArchived) }
        }
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            Timber.d("onNotificationPosted")
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            Timber.d("onNotificationCancelled")
        }
    }

    private inner class PodcastPlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Timber.d("onPlaybackStateChanged: $playbackState")
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Timber.d("Listener heard isPlaying: $isPlaying")
            viewModelScope.launch(ioDispatcher) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Timber.e("Error: ${error.message}")
        }
    }
}
