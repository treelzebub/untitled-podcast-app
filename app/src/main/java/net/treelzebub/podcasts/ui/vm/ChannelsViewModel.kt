package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.ChannelsRepo
import net.treelzebub.podcasts.ui.models.ChannelUi
import javax.inject.Inject

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val repo: ChannelsRepo
) : ViewModel() {

    fun listenForChannels(listener: (List<ChannelUi>) -> Unit) {
        viewModelScope.launch {
            repo.listenForChannels(listener)
        }
    }
}