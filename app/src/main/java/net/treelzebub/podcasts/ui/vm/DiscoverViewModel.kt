package net.treelzebub.podcasts.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.PreviousSearchRepo
import net.treelzebub.podcasts.net.PodcastIndexService
import net.treelzebub.podcasts.net.models.Feed
import javax.inject.Inject


@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val api: PodcastIndexService,
    private val previousSearchRepo: PreviousSearchRepo,
    private val podcastsRepo: PodcastsRepo
) : ViewModel() {

    data class SearchFeedsState(
        val feeds: List<Feed>,
        val error: String? = null
    ) {
        companion object {
            val Initial = SearchFeedsState(emptyList())
        }
    }

    private val _currentSearchState: MutableStateFlow<SearchFeedsState> = MutableStateFlow(SearchFeedsState(listOf()))
    val currentSearchState = _currentSearchState.asStateFlow()

    val previousSearches: Flow<List<String>> = previousSearchRepo.all()

    fun search(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("DiscoverViewModel", "Searching for: $query")
            previousSearchRepo.insert(query)

            try {
                val response = api.searchPodcasts(query)
                _currentSearchState.update { _currentSearchState.value.copy(feeds = response.feeds) }
            } catch (e: Exception) {
                Log.e("DiscoverViewModel", "API Error", e)
                _currentSearchState.update { _currentSearchState.value.copy(feeds = emptyList(), error = "Api Error.") }
            }
        }
    }

    fun select(feed: Feed, onError: (Exception) -> Unit) {
        Log.d("TEST", "Clicked on: ${feed.title}, with url: ${feed.url}")
        CoroutineScope(Dispatchers.IO).launch {
            podcastsRepo.fetchRssFeed(feed.url) {
                onError(it)
                // TODO actually handle errors
            }
        }
    }

    fun deletePreviousSearch(query: String) = previousSearchRepo.delete(query)
}