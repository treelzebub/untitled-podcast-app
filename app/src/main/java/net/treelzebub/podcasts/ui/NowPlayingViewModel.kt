package net.treelzebub.podcasts.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.DatabaseManager
import net.treelzebub.podcasts.data.RssParser
import net.treelzebub.podcasts.data.ui.ChannelUi
import net.treelzebub.podcasts.data.ui.toUi

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

    private val db = DatabaseManager(context)

    fun test(context: Context) {
        val rss = context.assets.open("test.rss").bufferedReader().use { it.readText() }
        viewModelScope.launch {
            val channel = RssParser().parseRss(rss)
            db.insert(channel.link!!, channel)

            val episodes = db.getEpisodesFromChannel(channel.link!!)
            Log.d("TEST", "DB insert complete. Got ${episodes.size} episodes!")
        }
    }

    fun listenForEpisodes(listener: (List<ChannelUi>) -> Unit) {
        db.episodeQueries.get_all_episodes().addListener {
            val map = db.getAllChannelsWithEpisodes()
            val channels = map.map {
                val channel = it.key
                val episodes = it.value
                ChannelUi(
                    channel.id,
                    channel.rss_link,
                    channel.title,
                    channel.link,
                    channel.description,
                    channel.image!!,
                    channel.lastBuildDate!!,
                    channel.itunesChannelData?.duration.orEmpty(),
                    episodes.toUi()
                )
            }
            listener(channels)
        }
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

    fun listen(fn: (Player) -> Unit) {
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                fn(player)
            }

            override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                super.onAvailableCommandsChanged(availableCommands)

            }
        })
    }

}