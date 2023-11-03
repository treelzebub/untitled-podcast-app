package net.treelzebub.podcasts.data

import net.treelzebub.podcasts.ui.models.ChannelUi
import net.treelzebub.podcasts.ui.models.toUi
import javax.inject.Inject

class ChannelsRepo @Inject constructor(
    private val db: DatabaseManager
) {

    fun listenForChannels(listener: (List<ChannelUi>) -> Unit) {
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