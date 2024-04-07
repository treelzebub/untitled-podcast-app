package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.PodcastUi
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : StatefulViewModel<SubscriptionsViewModel.State>(State()) {

    init {
        getAllPodcasts()
    }

    data class State(
        val loading: Boolean = true,
        val podcasts: List<PodcastUi> = emptyList()
    )

    private fun getAllPodcasts() {
        viewModelScope.launch {
            val podcastsFlow = withContext(Dispatchers.IO) {
                repo.getAllAsFlow()
            }
            podcastsFlow.collect { currentState ->
                _state.update {
                    it.copy(
                        loading = false,
                        podcasts = currentState
                    )
                }
            }
        }
    }

    fun addRssFeed(url: String, onError: (Exception) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.fetchRssFeed(url, onError)
        }
    }
}
