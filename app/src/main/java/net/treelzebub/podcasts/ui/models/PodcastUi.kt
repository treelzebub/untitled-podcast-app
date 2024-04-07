package net.treelzebub.podcasts.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class PodcastUi(
    val id: String,
    val link: String,
    val title: String,
    val description: String,
    val email: String,
    val imageUrl: String,
    val lastFetched: String,
    val rssLink: String
)