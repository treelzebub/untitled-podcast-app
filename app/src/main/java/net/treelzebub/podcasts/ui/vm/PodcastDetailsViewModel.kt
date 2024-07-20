package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
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
) : StatefulViewModel<PodcastDetailsViewModel.State>(State()) {

    @AssistedFactory
    interface Factory {
        fun create(podcastId: String): PodcastDetailsViewModel
    }

    private val showPlayed: Boolean get() = prefs.getBoolean(EpisodesShowPlayed(podcastId))

    init {
        getPodcastAndEpisodes()
    }

    @Stable
    data class State(
        val loading: Boolean = true,
        val podcast: PodcastUi? = null,
        val episodes: List<EpisodeUi> = listOf(),
        val showPlayed: Boolean = true,
        val queue: List<EpisodeUi> = listOf()
    )

    private fun getPodcastAndEpisodes() {
        if (!state.value.loading) loading()
        viewModelScope.launch {
            val showPlayed = showPlayed // anchor momentary value
            val podcastFlow = repo.getPodcast(podcastId)
            val episodesFlow = repo.getEpisodes(podcastId, showPlayed)
            val pair = podcastFlow.combine(episodesFlow) { podcast, episodes ->
                podcast to episodes
            }
            pair.collect {
                _state.update { state ->
                    state.copy(
                        loading = false,
                        podcast = it.first,
                        episodes = it.second,
                        showPlayed = showPlayed
                    )
                }
            }
        }
    }

    private fun refreshEpisodes() {
        if (!state.value.loading) loading()
        val showPlayed = showPlayed
        val episodes = repo.getEpisodesList(podcastId, showPlayed)
        _state.update {
            it.copy(
                loading = false,
                episodes = episodes,
                showPlayed = showPlayed
            )
        }
    }

    fun addToQueue(episode: EpisodeUi) {
        TODO()
    }

    fun addToQueue(index: Int, episode: EpisodeUi) {
        TODO()
    }

    fun onToggleShowPlayed() {
        Timber.d("onlyUnplayed: applying ${!showPlayed}")
        prefs.putBoolean(EpisodesShowPlayed(podcastId), !showPlayed)
        refreshEpisodes()
    }

    fun deletePodcast() {
        viewModelScope.launch {
            repo.deletePodcastById(podcastId)
        }
    }

    private fun loading() {
        _state.update { it.copy(loading = true) }
    }
}
