package net.treelzebub.podcasts.data

import com.prof18.rssparser.model.RssChannel


interface RssFeedHandler {
    suspend fun parse(feed: String): RssChannel
    suspend fun fetch(url: String): RssChannel
}
