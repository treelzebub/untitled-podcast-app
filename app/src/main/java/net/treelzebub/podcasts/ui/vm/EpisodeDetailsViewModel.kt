package net.treelzebub.podcasts.ui.vm

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.media.PlayerManager
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val playerManager: PlayerManager,
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
    private val _positionState = MutableStateFlow("00:00")
    val positionState = _positionState.asStateFlow()
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
    private val listener = PodcastPlayerListener()

    init {
        viewModelScope.launch {
            playerManager.init(getApplication(), listener)
            episode.collect {
                it?.let { playerManager.prepare(it, listener) }
            }
            loadEpisode(episodeId)
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            playerManager.removeListener(listener)
        }
        super.onCleared()
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
        viewModelScope.launch { playerManager.playPause() }
    }

    private fun download() {
        Timber.d("TODO: Download")
    }

    private fun addToQueue(id: String) = viewModelScope.launch {
        // TODO UI State -> isInQueue
        queueStore.add(repo.getEpisodeById(id)) { TODO() }
    }

    private inner class PodcastPlayerListener : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            viewModelScope.launch {
                playerManager.listenPosition { position, duration ->
                    _positionState.value = Strings.formatPosition(position, duration)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.e("Error: ${error.message}")
            super.onPlayerError(error)
        }
    }
}
