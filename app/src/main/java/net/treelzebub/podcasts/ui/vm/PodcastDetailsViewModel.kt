package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi


@HiltViewModel(assistedFactory = PodcastDetailsViewModel.Factory::class)
class PodcastDetailsViewModel @AssistedInject constructor(
    @Assisted val podcastId: String,
    private val repo: PodcastsRepo
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
        val episodes: List<EpisodeUi> = listOf(),
        val queue: List<EpisodeUi> = listOf()
    )

    private fun getPodcastAndEpisodes(podcastId: String) {
        viewModelScope.launch {
            repo.getPodcastWithEpisodes(podcastId).collect { pair ->
                _state.update {
                    it.copy(
                        loading = pair == null,
                        podcast = pair?.first,
                        episodes = pair?.second.orEmpty()
                    )
                }
            }
        }
    }

    fun addToQueue(episode: EpisodeUi) {

    }

    fun addToQueue(index: Int, episode: EpisodeUi) {

    }

    fun deletePodcast() {
        viewModelScope.launch {
            repo.deletePodcastById(podcastId)
        }
    }
}
