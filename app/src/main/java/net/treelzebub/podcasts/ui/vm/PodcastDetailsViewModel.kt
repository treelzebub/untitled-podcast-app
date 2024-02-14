package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import javax.inject.Inject

@HiltViewModel
class PodcastDetailsViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : StatefulViewModel<PodcastDetailsViewModel.PodcastDetailsState>(PodcastDetailsState()) {

    data class PodcastDetailsState(
        val loading: Boolean = true,
        val podcast: PodcastUi? = null,
        val episodes: List<EpisodeUi> = listOf()
    )

    fun getPodcastAndEpisodes(link: String) {
        viewModelScope.launch {
            val podcastFlow = repo.getPodcastByLink(link)
            val episodesFlow = repo.getEpisodesByChannelLink(link)
            podcastFlow.combine(episodesFlow) { podcast, episodes ->
                PodcastDetailsState(false, podcast, episodes)
            }.collectLatest { currentState ->
                _state.update {
                    it.copy(
                        loading = currentState.loading,
                        podcast = currentState.podcast,
                        episodes = currentState.episodes
                    )
                }
            }
        }
    }
}
