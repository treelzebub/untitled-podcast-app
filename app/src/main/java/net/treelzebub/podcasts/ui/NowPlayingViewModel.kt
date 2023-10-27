package net.treelzebub.podcasts.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import net.treelzebub.podcasts.media.PodcastPlayer

class NowPlayingViewModel(context: Context, uri: Uri) : ViewModel() {

    companion object {
        private val TAG = NowPlayingViewModel::class.java.simpleName
    }

    private val media = MediaItem.fromUri(uri)
    val player = ExoPlayer.Builder(context.applicationContext)
        .build()
        .apply {
            setMediaItem(media)
            prepare()
        }

    val play = {
        Log.d(TAG, "Playing...")
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }
    val stop = {
        Log.d(TAG, "Stopping...")
        player.stop()
    }

    fun listen(fn: (Player) -> Unit) {
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                fn(player)
            }
        })
    }

}