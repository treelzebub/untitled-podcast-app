package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            val currentStateFlow = withContext(Dispatchers.IO) {
                val podcastFlow = repo.getPodcastByLink(link)
                val episodesFlow = repo.getEpisodesByChannelLink(link)
                podcastFlow.combine(episodesFlow) { podcast, episodes ->
                    PodcastDetailsState(false, podcast, episodes)
                }
            }

            currentStateFlow.collect { currentState ->
                _state.update {
                    it.copy(
                        loading = false,
                        podcast = currentState.podcast,
                        episodes = currentState.episodes
                    )
                }
            }
        }
    }

    fun deletePodcast(link: String)  = repo.deletePodcastById(link)
}
