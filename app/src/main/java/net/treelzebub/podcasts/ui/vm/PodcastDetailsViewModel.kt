package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import timber.log.Timber


@HiltViewModel(assistedFactory = PodcastDetailsViewModel.Factory::class)
class PodcastDetailsViewModel @AssistedInject constructor(
    @Assisted val podcastId: String,
    private val repo: PodcastsRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StatefulViewModel<PodcastDetailsViewModel.State>(State()) {

    @AssistedFactory
    interface Factory {
        fun create(podcastId: String): PodcastDetailsViewModel
    }

    init {
        getPodcastAndEpisodes(podcastId)
    }

    @Stable
    data class State(
        val loading: Boolean = true,
        val podcast: PodcastUi? = null,
        val episodes: List<EpisodeUi> = listOf()
    )

    // TODO the repo should be doing most of this
    private fun getPodcastAndEpisodes(podcastId: String) {
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
                    Timber.d("Updated Pod Details State! ${currentState.episodes.size} Episodes.")
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
