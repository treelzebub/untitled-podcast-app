package net.treelzebub.podcasts.ui.models

data class ChannelUi(
    val id: String,
    val rssLink: String,
    val title: String,
    val link: String,
    val description: String,
    val image: String,
    val lastBuildDate: String,
    val duration: String,
    val episodes: List<EpisodeUi>
) {
    val episodeCount: Int
        get() = episodes.size
}
