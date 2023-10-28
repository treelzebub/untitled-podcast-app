package net.treelzebub.podcasts.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.ui.ChannelUi
import net.treelzebub.podcasts.data.ui.toUi

class EpisodesRepo(private val scope: CoroutineScope) {

    private val db = DatabaseManager

    fun test(context: Context) {
        val rss = context.assets.open("test.rss").bufferedReader().use { it.readText() }
        scope.launch {
            val channel = RssParser().parseRss(rss)
            db.insert(channel.link!!, channel)

            val episodes = db.getEpisodesFromChannel(channel.link!!)
            Log.d("TEST", "DB insert complete. Got ${episodes.size} episodes!")
        }
    }

    fun listenForEpisodes(listener: (List<ChannelUi>) -> Unit) {
        db.listenForEpisodes {
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
}