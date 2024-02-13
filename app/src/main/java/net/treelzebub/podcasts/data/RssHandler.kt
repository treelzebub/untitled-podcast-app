package net.treelzebub.podcasts.data

import com.prof18.rssparser.model.RssChannel


interface RssHandler {
    suspend fun parse(feed: String): RssChannel
    suspend fun fetch(url: String): RssChannel
}
