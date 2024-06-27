package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.AddToQueue
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Archive
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.ToggleBookmarked
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.ToggleHasPlayed
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.PlayPause
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Share


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
        ToggleBookmarked, Share, Download, AddToQueue, PlayPause, ToggleHasPlayed, Archive
    }

    data class EpisodeState(
        val episodeUi: EpisodeUi? = null,
        val loading: Boolean = episodeUi == null
    )

    init {
        getEpisode(episodeId)
    }

    val actionHandler: (EpisodeDetailAction) -> Unit = { action ->
        when (action) {
            ToggleBookmarked -> toggleBookmarked()
            Share -> share()
            Download -> download()
            AddToQueue -> addToQueue()
            PlayPause -> playPause()
            ToggleHasPlayed -> toggleHasPlayed()
            Archive -> archive()
        }
    }

    private val episode: EpisodeUi?
        get() = state.value.episodeUi

    private fun toggleBookmarked() {
        viewModelScope.launch {
            episode?.let { repo.setIsBookmarked(it.id, !it.isBookmarked) }
        }
    }

    private fun share() {

    }

    private fun playPause() {

    }

    private fun download() {

    }

    private fun addToQueue() {
        viewModelScope.launch {
            episode?.let { repo.addToQueue(it) { TODO() } }
        }
    }

    private fun toggleHasPlayed() {
        viewModelScope.launch {
            episode?.let { repo.setHasPlayed(it.id, !it.hasPlayed) }
        }
    }

    private fun archive() {
        viewModelScope.launch {
            episode?.let { repo.setIsArchived(it.id, !it.isArchived) }
        }
    }

    private fun getEpisode(episodeId: String) {
        viewModelScope.launch {
            repo.getEpisodeById(episodeId).collect { episode ->
                _state.update { it.copy(loading = false, episodeUi = episode) }
            }
        }
    }
}
