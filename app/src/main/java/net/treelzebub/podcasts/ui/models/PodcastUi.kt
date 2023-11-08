package net.treelzebub.podcasts.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class PodcastUi(
    public val link: String,
    public val title: String,
    public val description: String,
    public val email: String,
    public val imageUrl: String,
    public val lastFetched: String,
    public val rssLink: String
)