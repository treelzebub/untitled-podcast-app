package net.treelzebub.podcasts.data

import android.content.Context
import android.util.Log
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodesRepo @Inject constructor(
    private val db: DatabaseManager,
) {
    suspend fun test(context: Context) {
        val rss = context.assets.open("test.rss").bufferedReader().use { it.readText() }
        val channel = RssParser().parseRss(rss)
        db.insert(channel.link!!, channel)

        val episodes = db.getEpisodesFromChannel(channel.link!!)
        Log.d("TEST", "DB insert complete. Got ${episodes.size} episodes!")
    }

    fun listenForEpisodes(channelId: String, listener: (List<EpisodeUi>) -> Unit) {
        db.listenForEpisodes(channelId) {
            val raw = db.getEpisodesFromChannel(channelId)
            val episodes = raw.map {
                EpisodeUi(
                    it.id, it.channel_id, it.title, it.description.orEmpty(), it.date.orEmpty(), it.link,
                    it.streaming_link, it.image_url.orEmpty(), it.duration.orEmpty()
                )
            }
            listener(episodes)
        }
    }
}