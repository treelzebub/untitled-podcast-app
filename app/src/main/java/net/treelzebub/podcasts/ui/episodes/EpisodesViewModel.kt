package net.treelzebub.podcasts.ui.episodes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.treelzebub.podcasts.data.EpisodesRepo
import net.treelzebub.podcasts.data.ui.ChannelUi

class EpisodesViewModel : ViewModel() {

    private val repo = EpisodesRepo(viewModelScope)

    fun listenForEpisodes(listener: (List<ChannelUi>) -> Unit) = repo.listenForEpisodes(listener)
    fun test(context: Context) = repo.test(context)
}