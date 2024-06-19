package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val repo: PodcastsRepo,
    // TODO assisted inject podcastId
) : StatefulViewModel<EpisodesViewModel.State>(State()) {

    @Stable
    data class State(
        val loading: Boolean = false,
        val episodes: List<EpisodeUi> = emptyList()
    )

    fun getEpisodes(podcastId: String) {
        viewModelScope.launch {
            repo.getEpisodesByPodcastId(podcastId).collect {

            }
        }
    }
}