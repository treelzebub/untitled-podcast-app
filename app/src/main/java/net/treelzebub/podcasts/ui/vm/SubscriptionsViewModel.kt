package net.treelzebub.podcasts.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.App
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.RssParser
import net.treelzebub.podcasts.ui.models.PodcastUi
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : ViewModel() {

    init {
        viewModelScope.launch { test() }
    }

    data class PodcastsState(
        val podcasts: List<PodcastUi>
    ) {
        companion object {
            val Initial = PodcastsState(listOf())
        }
    }

    private val _state = MutableStateFlow(PodcastsState.Initial)
    val state = _state.asStateFlow()

    val podcasts = repo.getAllPodcasts()

    private suspend fun test() {
        val context = App.Instance
        val rss1 = context.assets.open("test.rss").bufferedReader().use { it.readText() }
        val rss2 = context.assets.open("srs.rss").bufferedReader().use { it.readText() }
        val channel1 = RssParser().parseRss(rss1)
        val channel2 = RssParser().parseRss(rss2)

        repo.upsert(channel1.link!!, channel1)
        repo.upsert(channel2.link!!, channel2)
        Log.d("TEST", "VM upserted pods")
    }
}