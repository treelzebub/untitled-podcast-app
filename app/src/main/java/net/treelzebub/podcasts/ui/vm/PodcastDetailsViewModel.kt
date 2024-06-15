package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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
    private val repo: PodcastsRepo,
    private val ioDispatcher: CoroutineDispatcher
) : StatefulViewModel<PodcastDetailsViewModel.State>(State()) {

    data class State(
        val loading: Boolean = true,
        val podcast: PodcastUi? = null,
        val episodes: List<EpisodeUi> = listOf()
    )

    // TODO the repo should be doing most of this
    fun getPodcastAndEpisodes(podcastId: String) {
        viewModelScope.launch {
            val currentStateFlow = withContext(ioDispatcher) {
                val podcastFlow = repo.getPodcastById(podcastId)
                val episodesFlow = repo.getEpisodesByPodcastId(podcastId)
                podcastFlow.combine(episodesFlow) { podcast, episodes ->
                    State(false, podcast, episodes)
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
