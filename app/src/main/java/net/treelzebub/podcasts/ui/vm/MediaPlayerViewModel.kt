package net.treelzebub.podcasts.ui.vm

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor(
    val player: Player,
    private val repo: PodcastsRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    init {
        player.prepare()
    }

    fun play(episodeId: String) {
        Log.d("TESTPLAYER", "play called")
        viewModelScope.launch {
            repo.getEpisodeById(episodeId).collect {
                Log.d("TESTPLAYER", "in the map")
                val uri = Uri.parse(it.streamingLink)
                val mediaItem = MediaItem.fromUri(uri)
                Log.d("TESTPLAYER", "Got media item - ${mediaItem.mediaId} from uri - $uri")
                player.playWhenReady = true
                player.setMediaItem(mediaItem)
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}