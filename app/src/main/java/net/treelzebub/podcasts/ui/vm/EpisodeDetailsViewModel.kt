package net.treelzebub.podcasts.ui.vm

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.media.PlayerManager
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.AddToQueue
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
    data class EpisodeState(
        val episode: EpisodeUi? = null
    )

    @Stable
    @Immutable
    data class MutableEpisodeState(
        val loading: Boolean = true,
        val queueIndex: Int = 0,
        val bufferedPercentage: Int = 0,
        val isPlaying: Boolean = false,
        val hasPlayed: Boolean = false,
        val isBookmarked: Boolean = false,
        val isArchived: Boolean = false
    )

    sealed class Action {
        data object ToggleBookmarked: Action()
        data class Share(val context: Context): Action()
        data object Download: Action()
        data object AddToQueue: Action()
        data object PlayPause: Action()
        data object ToggleHasPlayed: Action()
    }

    private val _episodeState = MutableStateFlow(EpisodeState())
    val episodeState = _episodeState.asStateFlow()
    private val _uiState = MutableStateFlow(MutableEpisodeState())
    val mutableState = _uiState.asStateFlow()
    private val _positionState = MutableStateFlow("00:00")
    val positionState = _positionState.asStateFlow()
    val actionHandler: OnClick<Action> = { action ->
        when (action) {
            ToggleBookmarked -> toggleBookmarked()
            is Share -> share(action.context)
            Download -> download()
            AddToQueue -> addToQueue(episodeId)
            PlayPause -> playPause()
            ToggleHasPlayed -> toggleHasPlayed()
        }
    }
    private val playerListener = PodcastPlayerListener()
    private val positionListener: (Long, Long) -> Unit = { position, duration ->
        _positionState.value = Strings.formatPosition(position, duration)
    }

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            playerManager.init(getApplication(), playerListener)
            loadEpisode()
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            playerManager.unlisten(playerListener)
        }
        super.onCleared()
    }

    private fun loadEpisode() {
        viewModelScope.launch(ioDispatcher) {
            val episodeFlow = repo.getEpisodeFlowById(episodeId)
            val episode = episodeFlow.first()
            _episodeState.update {
                it.copy(episode = episode)
            }
            episodeFlow.collect { updated ->
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

    private fun share(c: Context) {
        val url = episodeState.value.episode?.streamingLink ?: return
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }
        startActivity(c, Intent.createChooser(shareIntent, null), Bundle.EMPTY)
    }

    private fun playPause() {
        viewModelScope.launch {
            playerManager.prepareIfNeeded(episodeState.value.episode!!, playerListener) // FIXME this is hacky
            playerManager.playPause()
        }
    }

    private fun download() {
        Timber.d("TODO: Download")
    }

    private fun addToQueue(id: String) = viewModelScope.launch {
        // TODO UI State -> isInQueue
        queueStore.add(repo.getEpisodeById(id)) { TODO() }
    }

    private inner class PodcastPlayerListener : Player.Listener {

        override fun onIsLoadingChanged(isLoading: Boolean) {
            // TODO make this just change the play/pause button to a spinner
            // uiState.update { it.copy(loading = isLoading) }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            viewModelScope.launch {
                if (isPlaying) {
                    playerManager.listenPosition(block = positionListener)
                } else {
                    playerManager.withPlayer {
                        if (contentDuration - currentPosition <= 15_000L) {
                            viewModelScope.launch { repo.markPlayed(episodeId) }
                        }
                    }
                }
            }
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            viewModelScope.launch {
                playerManager.listenPosition(playbackParameters.speed, positionListener)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.e("Error: ${error.message}")
            super.onPlayerError(error)
        }
    }
}
