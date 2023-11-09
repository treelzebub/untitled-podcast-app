package net.treelzebub.podcasts.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : ViewModel() {

    fun getFlow(channelId: String): Flow<List<EpisodeUi>> = repo.getEpisodesByChannelLink(channelId)

    fun test(context: Context) {
        viewModelScope.launch {
            repo.test(context)
        }
    }
}