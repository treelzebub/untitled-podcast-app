package net.treelzebub.podcasts.ui.episodes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.EpisodesRepo
import net.treelzebub.podcasts.data.ui.ChannelUi
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val repo: EpisodesRepo
) : ViewModel() {

    fun listenForEpisodes(listener: (List<ChannelUi>) -> Unit) {
        viewModelScope.launch {
            repo.listenForEpisodes(listener)
        }
    }
    fun test(context: Context) {
        viewModelScope.launch {
            repo.test(context)
        }
    }
}