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
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.*

@HiltViewModel(assistedFactory = EpisodeDetailViewModel.Factory::class)
class EpisodeDetailViewModel @AssistedInject constructor(
    private val repo: PodcastsRepo,
    @Assisted private val episodeId: String
) : StatefulViewModel<EpisodeDetailViewModel.EpisodeState>(EpisodeState()) {

    @AssistedFactory
    interface Factory {
        fun create(episodeId: String): EpisodeDetailViewModel
    }

    enum class EpisodeDetailAction {
        Fave, Share, Download, AddToQueue, Play, MarkPlayed, Archive
    }

    data class EpisodeState(
        val loading: Boolean = true,
        val episodeUi: EpisodeUi? = null
    )

    init {
        getEpisode(episodeId)
    }

    val actionHandler: (EpisodeDetailAction) -> Unit = {
        when (it) {
            Fave -> fave()
            Share -> share()
            Download -> download()
            AddToQueue -> addToQueue()
            Play -> playPause()
            MarkPlayed -> markPlayed()
            Archive -> archive()
        }
    }

    private fun fave() {

    }

    private fun share() {

    }

    private fun playPause() {

    }

    private fun download() {

    }

    private fun addToQueue() {

    }

    private fun markPlayed() {

    }

    private fun archive() {

    }

    private fun getEpisode(episodeId: String) {
        viewModelScope.launch {
            repo.getEpisodeById(episodeId).collect { episode ->
                _state.update { it.copy(loading = false, episodeUi = episode) }
            }
        }
    }
}
