package net.treelzebub.podcasts.net

import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssChannel
import net.treelzebub.podcasts.data.RssHandler
import okhttp3.OkHttpClient
import javax.inject.Inject


class PodcastRssHandler @Inject constructor(client: OkHttpClient) : RssHandler {
    private val parser = RssParserBuilder(
        callFactory = client,
        charset = Charsets.UTF_8
    ).build()
    override suspend fun parse(feed: String): RssChannel = parser.parse(feed)
    override suspend fun fetch(url: String): RssChannel = parser.getRssChannel(url)
}
