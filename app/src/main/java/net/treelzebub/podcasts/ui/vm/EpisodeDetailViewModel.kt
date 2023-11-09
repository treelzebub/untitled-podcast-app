package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : StatefulViewModel<EpisodeDetailViewModel.EpisodeState>(EpisodeState()) {

    data class EpisodeState(
        val loading: Boolean = true,
        val episodeUi: EpisodeUi? = null
    )

    fun getEpisode(id: String) {
        viewModelScope.launch {
            repo.getEpisodeById(id).collectLatest { episode ->
                _state.update { it.copy(loading = false, episodeUi = episode) }
            }
        }
    }
}