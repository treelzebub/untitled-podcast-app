package net.treelzebub.podcasts.data

import net.treelzebub.podcasts.EpisodesQueries
import net.treelzebub.podcasts.PodcastsQueries
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.Time

fun EpisodesQueries.upsert(vararg episodes: EpisodeUi) {
    val anchor = this
    transaction {
        episodes.forEach {
            anchor.upsert(
                it.id, it.channelId, it.channelTitle, it.title, it.description, it.sortDate, it.link,
                it.streamingLink, it.imageUrl, it.duration
            )
        }
    }
}

fun PodcastsQueries.insert_or_replace(vararg podcasts: PodcastUi) {
    val anchor = this
    transaction {
        podcasts.forEach {
            anchor.insert_or_replace(
                it.id, it.link, it.title, it.description, it.email, it.imageUrl,
                Time.zonedEpochSeconds(it.lastBuildDate), it.rssLink, it.lastLocalUpdate
            )
        }
    }
}
