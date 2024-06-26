package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi

@HiltViewModel(assistedFactory = EpisodeDetailViewModel.Factory::class)
class EpisodeDetailViewModel @AssistedInject constructor(
    private val repo: PodcastsRepo,
    @Assisted private val episodeId: String
) : StatefulViewModel<EpisodeDetailViewModel.EpisodeState>(EpisodeState()) {

    @AssistedFactory
    interface Factory {
        fun create(episodeId: String): EpisodeDetailViewModel
    }

    data class EpisodeState(
        val loading: Boolean = true,
        val episodeUi: EpisodeUi? = null
    )

    init {
        getEpisode(episodeId)
    }

    fun getEpisode(episodeId: String) {
        viewModelScope.launch {
            repo.getEpisodeById(episodeId).collectLatest { episode ->
                _state.update { it.copy(loading = false, episodeUi = episode) }
            }
        }
    }
}