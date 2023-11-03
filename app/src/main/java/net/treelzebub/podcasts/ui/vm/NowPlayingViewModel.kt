package net.treelzebub.podcasts.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class NowPlayingViewModel(context: Context, uri: String) : ViewModel() {

    companion object {
        private val TAG = NowPlayingViewModel::class.java.simpleName
    }

    private val media = MediaItem.Builder().setUri(uri).setMimeType("audio/mpeg").build()
    private val player = ExoPlayer.Builder(context.applicationContext)
        .build()
        .apply {
            setMediaItem(media)
            prepare()
        }

    val play = {
        Log.d(TAG, "Play/Pause...")
        player.availableCommands
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }
    val stop = {
        Log.d(TAG, "Stopping...")
        player.stop()
        player.release()
    }

    fun listenForPlayerEvents(fn: (Player) -> Unit) {
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                fn(player)
            }
        })
    }

}