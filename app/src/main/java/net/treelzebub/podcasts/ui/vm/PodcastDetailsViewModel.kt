package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastPref.EpisodesShowPlayed
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.Prefs
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import timber.log.Timber


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

    @Stable
    data class State(
        val loading: Boolean = true,
        val podcast: PodcastUi? = null,
        val episodes: List<EpisodeUi> = listOf(),
        val showPlayed: Boolean = false,
        val queue: List<EpisodeUi> = listOf()
    )

    private val showPlayedFlow = prefs.booleanFlow(EpisodesShowPlayed(podcastId))
    private val podcastFlow = repo.getPodcast(podcastId)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), replay = 1)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val episodesFlow: StateFlow<List<EpisodeUi>> = showPlayedFlow.flatMapLatest { showPlayed ->
        flow { emit(repo.getEpisodesList(podcastId, showPlayed)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val uiState: StateFlow<State> = combine(podcastFlow, episodesFlow, showPlayedFlow) {
        podcast, episodes, showPlayed ->
        State(
            loading = false,
            podcast = podcast,
            episodes = episodes,
            showPlayed = showPlayed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State(loading = true))

    fun onToggleShowPlayed() {
        viewModelScope.launch {
            val currentShowPlayed = prefs.getBoolean(EpisodesShowPlayed(podcastId))
            Timber.d("old showPlayed: $currentShowPlayed")
            prefs.putBoolean(EpisodesShowPlayed(podcastId), !currentShowPlayed)
        }
    }

    fun deletePodcast() {
        viewModelScope.launch {
            repo.deletePodcastById(podcastId)
        }
    }
}
