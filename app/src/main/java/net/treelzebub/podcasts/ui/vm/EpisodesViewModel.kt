package net.treelzebub.podcasts.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.EpisodesRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val repo: EpisodesRepo
) : ViewModel() {

    fun listenForEpisodes(channelId: String, listener: (List<EpisodeUi>) -> Unit) {
        viewModelScope.launch {
            repo.listenForEpisodes(channelId, listener)
        }
    }
    fun test(context: Context) {
        viewModelScope.launch {
            repo.test(context)
        }
    }
}