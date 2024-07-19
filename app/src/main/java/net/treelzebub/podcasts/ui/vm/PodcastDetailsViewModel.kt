package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastPref.EpisodesFilterUnplayed
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.Prefs
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import timber.log.Timber


@HiltViewModel(assistedFactory = PodcastDetailsViewModel.Factory::class)
class PodcastDetailsViewModel @AssistedInject constructor(
    @Assisted private val podcastId: String,
    private val repo: PodcastsRepo,
    private val prefs: Prefs
) : StatefulViewModel<PodcastDetailsViewModel.State>(State()) {

    @AssistedFactory
    interface Factory {
        fun create(podcastId: String): PodcastDetailsViewModel
    }

    private val onlyUnplayed: Boolean
        get() = prefs.getBoolean(EpisodesFilterUnplayed(podcastId))

    init {
        getPodcastAndEpisodes()
    }

    @Stable
    data class State(
        val loading: Boolean = true,
        val podcast: PodcastUi? = null,
        val episodes: List<EpisodeUi> = listOf(),
        val onlyUnplayed: Boolean = true,
        val queue: List<EpisodeUi> = listOf()
    )

    private fun getPodcastAndEpisodes() {
        if (!state.value.loading) loading()
        viewModelScope.launch {
            repo.getPodcastWithEpisodes(podcastId, onlyUnplayed).collect { pair ->
                _state.update {
                    it.copy(
                        loading = pair == null,
                        podcast = pair?.first,
                        episodes = pair?.second.orEmpty(),
                        onlyUnplayed = onlyUnplayed
                    )
                }
            }
        }
    }

    private fun refreshEpisodes() {
        loading()
        viewModelScope.launch {
            val anchor = onlyUnplayed
            Timber.d("onlyUnplayed: refreshing with value $anchor")
            _state.update { it.copy(loading = false, episodes = repo.getEpisodes(podcastId, anchor)) }
        }
    }

    fun addToQueue(episode: EpisodeUi) {
        TODO()
    }

    fun addToQueue(index: Int, episode: EpisodeUi) {
        TODO()
    }

    fun toggleOnlyUnplayed() {
        Timber.d("onlyUnplayed: applying ${!onlyUnplayed}")
        prefs.putBoolean(EpisodesFilterUnplayed(podcastId), !onlyUnplayed)
        refreshEpisodes()
    }

    fun deletePodcast() {
        viewModelScope.launch {
            repo.deletePodcastById(podcastId)
        }
    }

    private fun loading() {
        _state.update { it.copy(loading = true) }
    }
}
