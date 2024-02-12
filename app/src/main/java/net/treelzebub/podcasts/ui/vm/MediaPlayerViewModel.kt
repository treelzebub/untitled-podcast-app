package net.treelzebub.podcasts.ui.vm

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor(
    val player: Player,
    private val repo: PodcastsRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    init {
        player.prepare()
        player.playWhenReady = true
    }

    fun play(episodeId: String) {
        viewModelScope.launch {
            repo.getEpisodeById(episodeId).collect {
                val uri = Uri.parse(it.streamingLink)
                val mediaItem = MediaItem.fromUri(uri)
                player.setMediaItem(mediaItem)
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}