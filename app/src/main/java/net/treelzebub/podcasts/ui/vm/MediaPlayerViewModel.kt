package net.treelzebub.podcasts.ui.vm

import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor(
    private val repo: PodcastsRepo,
) : StatefulViewModel<MediaPlayerViewModel.MediaState>(MediaState()) {

    data class MediaState(
        val loading: Boolean = true,
        val mediaItem: MediaItem? = null
    )

    fun play(episodeId: String) {
        viewModelScope.launch {
            repo.getEpisodeById(episodeId).collect {
                val uri = Uri.parse(it.streamingLink)
                val mediaItem = MediaItem.fromUri(uri)
                _state.update { state -> state.copy(loading = false, mediaItem = mediaItem) }
            }
        }
    }
}