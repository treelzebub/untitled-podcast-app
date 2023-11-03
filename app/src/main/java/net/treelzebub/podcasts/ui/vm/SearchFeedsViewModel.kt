package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.net.PodcastIndexService
import net.treelzebub.podcasts.net.models.Feed
import javax.inject.Inject

@HiltViewModel
class SearchFeedsViewModel @Inject constructor(
    private val api: PodcastIndexService
) : ViewModel() {

    data class SearchFeedsState(
        val feeds: List<Feed>
    )

    private val _state: MutableStateFlow<SearchFeedsState> = MutableStateFlow(SearchFeedsState(listOf()))
    private val state = _state.asStateFlow()

    fun observe(fn: (SearchFeedsState) -> Unit) = state.onEach(fn)

    fun search(query: String) {
        viewModelScope.launch {
            val response = api.searchPodcasts(query)
            _state.update { it.copy(feeds = response.feeds) }
        }
    }
}