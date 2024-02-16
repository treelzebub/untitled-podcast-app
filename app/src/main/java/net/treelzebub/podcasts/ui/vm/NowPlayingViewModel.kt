package net.treelzebub.podcasts.ui.vm

import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val repo: PodcastsRepo,
) : StatefulViewModel<NowPlayingViewModel.State>(State()) {

    data class State(
        val loading: Boolean = true,
        val mediaItem: MediaItem? = null
    )

    fun play(episodeId: String) {
        viewModelScope.launch {
            val episode = withContext(Dispatchers.IO) {
                repo.getEpisodeById(episodeId)
            }
            episode.collect {
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(it.streamingLink))
                    .setMediaId(it.id)
                    .setMediaMetadata(MediaMetadata.Builder()
                        .setArtist(it.channelTitle)
                        .setTitle(it.title)
                        .setDescription(it.description)
                        .setArtworkUri(Uri.parse(it.imageUrl))
                        .setIsPlayable(true)
                        .build())
                    .build()
                _state.update { state -> state.copy(loading = false, mediaItem = mediaItem) }
            }
        }
    }
}