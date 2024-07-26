package net.treelzebub.podcasts.net.models

import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast

data class RssChannelResponse(
    val rssLink: String,
    val podcast: Podcast,
    val episodes: List<Episode>
)
