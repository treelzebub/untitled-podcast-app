package net.treelzebub.podcasts.util

import com.prof18.rssparser.model.RssChannel
import net.treelzebub.podcasts.data.RssHandler


class StubRssHandler : RssHandler {
    override suspend fun parse(feed: String): RssChannel {
        TODO("Not yet implemented")
    }

    override suspend fun fetch(url: String): RssChannel {
        TODO("Not yet implemented")
    }
}
