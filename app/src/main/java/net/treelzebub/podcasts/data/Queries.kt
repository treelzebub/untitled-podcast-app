package net.treelzebub.podcasts.data

import net.treelzebub.podcasts.EpisodesQueries
import net.treelzebub.podcasts.PodcastsQueries
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.Time

fun EpisodesQueries.upsert(episode: EpisodeUi) {
    val anchor = this
    with(episode) {
        anchor.upsert(id, channelId, channelTitle, title, description, sortDate, link,
            streamingLink, imageUrl, duration)
    }
}

fun PodcastsQueries.insert_or_replace(podcast: PodcastUi) {
    val anchor = this
    with(podcast) {
        anchor.insert_or_replace(id, link, title, description, email, imageUrl,
            Time.zonedEpochMillis(lastBuildDate), rssLink, lastLocalUpdate)
    }
}
