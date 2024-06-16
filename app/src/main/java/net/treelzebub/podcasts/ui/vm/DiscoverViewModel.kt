package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.SearchQueriesRepo
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.net.PodcastIndexService
import net.treelzebub.podcasts.net.models.Feed
import javax.inject.Inject


@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val api: PodcastIndexService,
    private val queriesRepo: SearchQueriesRepo,
    private val podcastsRepo: PodcastsRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StatefulViewModel<DiscoverViewModel.State>(State()) {

    data class State(
        val loading: Boolean = true,
        val previousQueries: List<String> = emptyList(),
        val feeds: List<Feed> = emptyList(),
        val error: String? = null
    )

    init {
        getPreviousQueries()
    }

    fun search(query: String) {
        loading()
        viewModelScope.launch {
            loading()
            val results = withContext(ioDispatcher) {
                queriesRepo.insert(query)
                api.searchPodcasts(query)
            }
            _state.update {
                it.copy(
                    loading = false,
                    feeds = results.feeds
                )
            }
        }
    }

    fun select(feed: Feed, onError: (Exception) -> Unit) {
        loading()
        CoroutineScope(ioDispatcher).launch {
            podcastsRepo.fetchRssFeed(feed.url) { onError(it) }
        }
    }

    fun deletePreviousSearch(query: String) = queriesRepo.delete(query)

    private fun getPreviousQueries() {
        viewModelScope.launch {
            val queriesFlow = withContext(ioDispatcher) {
                queriesRepo.all()
            }
            queriesFlow.collect { queries ->
                _state.update {
                    it.copy(
                        loading = false,
                        previousQueries = queries,
                    )
                }
            }
        }
    }

    private fun loading() = _state.update { it.copy(loading = true) }

    private fun error() {
        TODO()
    }
}