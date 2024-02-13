package net.treelzebub.podcasts.data

import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssChannel


class PodcastRssHandler : RssHandler {
    private val parser = RssParser()
    override suspend fun parse(feed: String): RssChannel = parser.parse(feed)
    override suspend fun fetch(url: String): RssChannel = parser.getRssChannel(url)
}
