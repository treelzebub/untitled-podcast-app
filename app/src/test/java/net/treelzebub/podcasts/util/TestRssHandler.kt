package net.treelzebub.podcasts.util

import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssChannel
import net.treelzebub.podcasts.data.RssHandler

/**
 * Parses real RSS/XML, unlike [StubRssHandler]. Used where sync tests need
 * [net.treelzebub.podcasts.data.PodcastsRepo.parseRssFeed] to produce a real Podcast/Episode pair.
 */
class TestRssHandler : RssHandler {
    private val parser = RssParserBuilder().build()

    override suspend fun parse(feed: String): RssChannel = parser.parse(feed)

    override suspend fun fetch(url: String): RssChannel {
        throw UnsupportedOperationException("fetch() is not used by sync tests")
    }
}
