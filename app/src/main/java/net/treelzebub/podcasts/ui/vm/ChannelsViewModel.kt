package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.PodcastUi
import javax.inject.Inject

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val repo: PodcastsRepo
) : ViewModel() {

    fun listenForChannels(listener: (List<PodcastUi>) -> Unit) {
        viewModelScope.launch {
            repo.listenForChannels(listener)
        }
    }
}