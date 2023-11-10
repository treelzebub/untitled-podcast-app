package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : ViewModel() {

    fun getFlow(channelId: String): Flow<List<EpisodeUi>> = repo.getEpisodesByChannelLink(channelId)
}