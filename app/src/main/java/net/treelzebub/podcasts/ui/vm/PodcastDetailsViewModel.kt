package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastPref.EpisodesShowPlayed
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.Prefs
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import timber.log.Timber


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = PodcastDetailsViewModel.Factory::class)
class PodcastDetailsViewModel @AssistedInject constructor(
    @Assisted private val podcastId: String,
    private val repo: PodcastsRepo,
    private val prefs: Prefs
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(podcastId: String): PodcastDetailsViewModel
    }

    @Stable @Immutable
    data class State(
        val loading: Boolean = true,
        val podcast: PodcastUi? = null,
        val episodes: List<EpisodeUi> = emptyList(),
        val showPlayed: Boolean = false,
        val queue: List<EpisodeUi> = emptyList()
    )

    enum class Action {
        DeletePodcast, ToggleShowPlayed
    }

    private val showPlayedFlow = prefs.booleanFlow(EpisodesShowPlayed(podcastId))
    private val podcastFlow = repo.getPodcast(podcastId)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), replay = 1)
    private val episodesFlow: StateFlow<List<EpisodeUi>> = showPlayedFlow.flatMapLatest { showPlayed ->
        repo.getEpisodesFlow(podcastId, showPlayed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val uiState: StateFlow<State> = combine(podcastFlow, episodesFlow, showPlayedFlow) { podcast, episodes, showPlayed ->
        State(
            loading = podcast == null,
            podcast = podcast,
            episodes = episodes,
            showPlayed = showPlayed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())

    val actionHandler: OnClick<Action> = { action ->
        when (action) {
            Action.DeletePodcast -> deletePodcast()
            Action.ToggleShowPlayed -> toggleShowPlayed()
        }
    }

    private fun toggleShowPlayed() {
        viewModelScope.launch {
            val currentShowPlayed = prefs.getBoolean(EpisodesShowPlayed(podcastId))
            Timber.d("old showPlayed: $currentShowPlayed")
            prefs.putBoolean(EpisodesShowPlayed(podcastId), !currentShowPlayed)
        }
    }

    private fun deletePodcast() {
        viewModelScope.launch {
            repo.deletePodcastById(podcastId)
        }
    }
}
