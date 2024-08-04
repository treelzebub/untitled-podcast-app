package net.treelzebub.podcasts.ui.vm

import android.app.Application
import android.content.ComponentName
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.util.concurrent.MoreExecutors
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.di.DefaultDispatcher
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.service.PlaybackService
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.AddToQueue
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.Archive
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.PlayPause
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.Share
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.ToggleBookmarked
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.ToggleHasPlayed
import net.treelzebub.podcasts.util.Strings
import timber.log.Timber


@UnstableApi
@HiltViewModel(assistedFactory = EpisodeDetailsViewModel.Factory::class)
class EpisodeDetailsViewModel @AssistedInject constructor(
    @Assisted private val episodeId: String,
    app: Application,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val repo: PodcastsRepo,
    private val queueStore: QueueStore
) : AndroidViewModel(app) {

    @AssistedFactory
    interface Factory {
        fun create(episodeId: String): EpisodeDetailsViewModel
    }

    @Stable
    @Immutable
    data class UiState(
        val loading: Boolean = true,
        val queueIndex: Int = 0,
        val bufferedPercentage: Int = 0,
        val isPlaying: Boolean = false,
        val hasPlayed: Boolean = false,
        val isBookmarked: Boolean = false,
        val isArchived: Boolean = false
    )

    enum class Action {
        ToggleBookmarked, Share, Download, AddToQueue, PlayPause, ToggleHasPlayed, Archive
    }

    val episode: Flow<EpisodeUi?> = flow {
        emit(repo.getEpisodeById(episodeId))
    }
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _positionState = MutableStateFlow("")
    val positionState = _positionState.asStateFlow()

    val player = mutableStateOf<Player?>(null)
    val actionHandler: OnClick<Action> = { action ->
        when (action) {
            ToggleBookmarked -> toggleBookmarked()
            Share -> share()
            Download -> download()
            AddToQueue -> addToQueue(episodeId)
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
    private val listener = PodcastPlayerListener()
    private val notifListener = object : PlayerNotificationManager.NotificationListener {}

    init {
        init(episodeId)
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        player.value = null
        super.onCleared()
    }

    private fun init(episodeId: String) {
        viewModelScope.launch {
            episode.collect {
                it ?: return@collect
                queueStore.add(it) { Timber.e("Error adding to queue") }
            }
        }

        with(controllerFuture) {
            addListener({
                if (isDone) {
                    player.value = controller!!
                    loadEpisode(episodeId)
                    viewModelScope.launch(mainDispatcher) { prepare(episodeId) }
                }
            }, MoreExecutors.directExecutor())
        }
    }

    private fun loadEpisode(episodeId: String) {
        viewModelScope.launch(ioDispatcher) {
            repo.getEpisodeFlowById(episodeId).collect { updated ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        queueIndex = queueStore.indexFor(updated.id),
                        hasPlayed = updated.hasPlayed,
                        isBookmarked = updated.isBookmarked,
                        isArchived = updated.isArchived
                    )
                }
            }
        }.invokeOnCompletion { error ->
            error?.let { Timber.e(it) }
        }
    }

    private fun prepare(episodeId: String) {
        viewModelScope.launch(ioDispatcher) {
            queueStore.stateFlow.collect { queue ->
                if (controller == null) return@collect
                val mediaItems = queue.asMediaItems()
                withContext(mainDispatcher) {
                    with(controller!!) {
                        addListener(listener)
                        sessionExtras.putString(PlaybackService.KEY_EPISODE_ID, episodeId)
                        setMediaItems(mediaItems, 0, queue[uiState.value.queueIndex].positionMillis) // TODO possible race condition?
                        playWhenReady = true
                        prepare()
                    }
                }
            }
        }
    }

    private fun toggleBookmarked() {
        viewModelScope.launch { repo.toggleIsBookmarked(episodeId) }
    }

    private fun toggleHasPlayed() {
        viewModelScope.launch { repo.toggleHasPlayed(episodeId) }
    }

    private fun toggleArchived() {
        viewModelScope.launch { repo.toggleIsArchived(episodeId) }
    }

    private fun share() {
        Timber.d("TODO: Share")
    }

    private fun playPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    private fun download() {
        Timber.d("TODO: Download")
    }

    private fun addToQueue(id: String) = viewModelScope.launch {
        // TODO UI State -> isInQueue
        repo.addToQueue(id) { TODO() }
    }

    private inner class PodcastPlayerListener : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }

            if (isPlaying) {
                viewModelScope.launch(defaultDispatcher) {
                    val interval = 1000L
                    val offset = withContext(mainDispatcher) {
                        val player = player.value!!
                        interval - (player.currentPosition % interval)
                    }
                    delay(offset)

                    while (true) {
                        val pair = withContext(mainDispatcher) {
                            val player = player.value!!
                            val currentPosition = player.currentPosition
                            val duration = player.contentDuration
                            currentPosition to duration
                        }
                        _positionState.emit(Strings.formatPosition(pair.first, pair.second))
                        delay(interval)
                    }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.e("Error: ${error.message}")
            super.onPlayerError(error)
        }
    }
}
