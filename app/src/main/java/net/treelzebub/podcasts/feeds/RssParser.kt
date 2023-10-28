package net.treelzebub.podcasts.feeds

import android.net.Uri
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssChannel

class RssParser {

    private val parser = RssParser()

    suspend fun parseRss(string: String) {
        parser.parse(string)
    }

    suspend fun getRssChannel(url: String): RssChannel {
        return parser.getRssChannel(url)
    }
}