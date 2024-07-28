package net.treelzebub.podcasts.data

import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.EpisodesQueries
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.PodcastsQueries


fun PodcastsQueries.upsert(podcast: Podcast) {
    with(podcast) {
        upsert(
            id = id,
            link = link,
            title = title,
            description = description,
            email = email,
            image_url = image_url,
            last_build_date = last_build_date,
            rss_link = rss_link,
            last_local_update = last_local_update,
            latest_episode_timestamp = latest_episode_timestamp,
        )
    }
}

fun EpisodesQueries.upsert(episode: Episode, podImageUrl: String) {
    with(episode) {
        upsert(
            id = id,
            podcast_id = podcast_id,
            podcast_title = podcast_title,
            title = title,
            description = description,
            date = date,
            link = link,
            streaming_link = streaming_link,
            local_file_uri = local_file_uri,
            image_url = if (image_url.isNullOrBlank()) podImageUrl else image_url,
            duration = duration,
            has_played = has_played,
            position_millis = position_millis,
            is_bookmarked = is_bookmarked,
            is_archived = is_archived
        )
    }
}

fun EpisodesQueries.upsert(episodes: List<Episode>, podImageUrl: String) = episodes.forEach { upsert(it, podImageUrl) }
