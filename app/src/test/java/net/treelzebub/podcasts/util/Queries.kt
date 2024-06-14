package net.treelzebub.podcasts.util

import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.EpisodesQueries
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.PodcastsQueries

fun EpisodesQueries.upsert(episode: Episode) {
    val anchor = this
    with(episode) {
        anchor.upsert(id, channel_id, channel_title, title, description, date, link,
            streaming_link, image_url, duration)
    }
}

fun PodcastsQueries.insert_or_replace(podcast: Podcast) {
    val anchor = this
    with(podcast) {
        anchor.insert_or_replace(id, link, title, description, email, image_url,
            last_build_date, rss_link, last_local_update)
    }
}