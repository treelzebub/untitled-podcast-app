package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.App
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.PodcastRssHandler
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : ViewModel() {

    companion object {
        private const val TIMEOUT = 5000L
    }

    val podcasts = repo.getAllPodcasts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT), emptyList())

    fun addRssFeed(url: String, onError: (Exception) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.fetchRssFeed(url, onError)
        }
    }
}
