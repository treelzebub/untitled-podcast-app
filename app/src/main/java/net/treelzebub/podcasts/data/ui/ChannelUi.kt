package net.treelzebub.podcasts.data.ui

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
)