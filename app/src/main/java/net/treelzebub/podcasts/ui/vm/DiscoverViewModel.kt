package net.treelzebub.podcasts.ui.vm

import android.webkit.URLUtil
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.SearchQueriesRepo
import net.treelzebub.podcasts.net.models.Feed
import net.treelzebub.podcasts.util.ErrorHandler
import javax.inject.Inject


@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val queriesRepo: SearchQueriesRepo,
    private val podcastsRepo: PodcastsRepo
) : StatefulViewModel<DiscoverViewModel.State>(State()) {

    @Stable
    data class State(
        val loading: Boolean = true,
        val previousQueries: List<String> = emptyList(),
        val feeds: List<Feed> = emptyList(),
        val error: String? = null
    )

    init {
        getPreviousQueries()
    }

    fun search(query: String?, onAdd: () -> Unit) {
        val clean = query?.trim().orEmpty()
        if (clean.isBlank()) return
        viewModelScope.launch {
            loading()
            if (URLUtil.isValidUrl(clean)) {
                podcastsRepo.fetchRssFeed(clean) { TODO("Error handling") }
                onAdd()
            } else {
                val results = queriesRepo.searchPodcasts(clean)
                _state.update {
                    it.copy(loading = false, feeds = results.feeds)
                }
            }
        }
    }

    fun select(feed: Feed, onAdd: () -> Unit, onError: ErrorHandler) {
        loading()
        viewModelScope.launch {
            podcastsRepo.fetchRssFeed(feed.url) {
                onError(it)
                error()
            }
            onAdd()
        }
    }

    fun clearFeeds() {
        _state.update { it.copy(feeds = emptyList()) }
    }

    fun deletePreviousSearch(query: String) = viewModelScope.launch {
        queriesRepo.delete(query)
    }

    private fun getPreviousQueries() {
        viewModelScope.launch {
            loading()
            queriesRepo.all().collect { queries ->
                _state.update {
                    it.copy(
                        loading = false,
                        previousQueries = queries.reversed(),
                    )
                }
            }
        }
    }

    private fun loading() = _state.update { it.copy(loading = true) }

    private fun error() {
        TODO("Handle error and inform user.")
    }
}