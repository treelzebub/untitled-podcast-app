package net.treelzebub.podcasts.util

import net.treelzebub.podcasts.EpisodesQueries
import net.treelzebub.podcasts.PodcastsQueries
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi

fun EpisodesQueries.upsert(vararg episodes: EpisodeUi) {
    transaction {
        episodes.forEach {
            upsert(
                it.id, it.podcastId, it.podcastTitle, it.title, it.description, it.sortDate, it.link,
                it.streamingLink, it.localFileUri, it.imageUrl, it.duration, it.hasPlayed, it.positionMillis,
                it.isBookmarked, it.isArchived
            )
        }
    }
}

fun PodcastsQueries.upsert(vararg podcasts: PodcastUi) {
    transaction {
        podcasts.forEach {
            upsert(
                it.id, it.link, it.title, it.description, it.email, it.imageUrl,
                Time.zonedEpochSeconds(it.lastBuildDate), it.rssLink, it.lastLocalUpdate,
                it.latestEpisodeTimestamp
            )
        }
    }
}
