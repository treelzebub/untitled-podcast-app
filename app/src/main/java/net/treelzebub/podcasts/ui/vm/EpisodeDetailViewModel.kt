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
import com.google.common.util.concurrent.MoreExecutors
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
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.Action.AddToQueue
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.Action.Archive
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.Action.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.Action.PlayPause
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.Action.Share
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.Action.ToggleBookmarked
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.Action.ToggleHasPlayed
import timber.log.Timber


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

    enum class Action {
        ToggleBookmarked, Share, Download, AddToQueue, PlayPause, ToggleHasPlayed, Archive
    }

    private val _uiState = MutableStateFlow(UiState())
    private val _episodeState = MutableStateFlow(EpisodeState())
    private val episodeHolder = MutableStateFlow<EpisodeUi?>(null)
    private val listener = PodcastPlayerListener()

    val uiState = _uiState.asStateFlow()
    val episodeState = _episodeState.asStateFlow()
    val player = mutableStateOf<Player?>(null)

    val actionHandler: OnClick<Action> = { action ->
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
        init(episodeId)
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        player.value = null
        super.onCleared()
    }

    private fun init(episodeId: String) {
        with(controllerFuture) {
            addListener({
                if (isDone) {
                    player.value = controller!!
                    loadEpisode(episodeId)
                }
            }, MoreExecutors.directExecutor())
        }
        prepare(episodeId)
    }

    private fun loadEpisode(episodeId: String) {
        viewModelScope.launch(ioDispatcher) {
            val episode = repo.getEpisodeById(episodeId)
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

                queueStore.add(episode) { Timber.e("Error adding to queue") }

                _uiState.update {
                    it.copy(
                        loading = false,
                        queueIndex = queueStore.indexFor(id),
                        progressMillis = positionMillis,
                        isBookmarked = isBookmarked,
                        isArchived = isArchived
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
                        setMediaItems(mediaItems, 0, queue[0].positionMillis) // TODO!
                        playWhenReady = true
                        prepare()
                    }
                }
            }
        }
    }

    private fun toggleBookmarked() {
        episodeHolder.value?.let { repo.setIsBookmarked(it.id, !it.isBookmarked) }
        _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }
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
        // TODO UI State -> isInQueue
        episodeHolder.value?.let { repo.addToQueue(it) { TODO() } }
    }

    private fun toggleHasPlayed() {
        episodeHolder.value?.let { repo.setHasPlayed(it.id, !it.hasPlayed) }
        _uiState.update { it.copy(hasPlayed = !it.hasPlayed) }
    }

    private fun toggleArchived() {
        episodeHolder.value?.let { repo.setIsArchived(it.id, !it.isArchived) }
        _uiState.update { it.copy(isArchived = !it.isArchived) }
    }

    private inner class PodcastPlayerListener : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.e("Error: ${error.message}")
            super.onPlayerError(error)
        }
    }
}
