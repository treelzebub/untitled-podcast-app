package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.App
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.RssParser
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : ViewModel() {

    companion object {
        private const val TIMEOUT = 5000L
    }

    init {
        viewModelScope.launch { test() }
    }

    val podcasts = repo.getAllPodcasts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT), emptyList())

    private suspend fun test() {
        val context = App.Instance
        val rss1 = context.assets.open("test.rss").bufferedReader().use { it.readText() }
        val rss2 = context.assets.open("srs.rss").bufferedReader().use { it.readText() }
        val channel1 = RssParser().parseRss(rss1)
        val channel2 = RssParser().parseRss(rss2)

        repo.upsert(channel1.link!!, channel1)
        repo.upsert(channel2.link!!, channel2)
    }

    fun addRssFeed(input: String) {

    }
}