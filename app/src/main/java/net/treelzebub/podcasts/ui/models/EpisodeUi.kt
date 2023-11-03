package net.treelzebub.podcasts.ui.models

import net.treelzebub.podcasts.Episode

data class EpisodeUi(
    val podcastName: String,
    val imageUrl: String,
    val title: String,
    val date: String,
    val description: String,
    val streamingUrl: String
)

fun List<Episode>.toUi() = map { it.toUi() }

fun Episode.toUi(): EpisodeUi {
    return EpisodeUi(
        channel_title,
        image!!,
        title,
        pubDate!!,
        description!!,
        link
    )
}