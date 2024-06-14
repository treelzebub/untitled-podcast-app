package net.treelzebub.podcasts.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class EpisodeUi(
    val id: String,
    val channelId: String,
    val channelTitle: String,
    val title: String,
    val description: String,
    val date: String,
    val link: String,
    val streamingLink: String,
    val imageUrl: String,
    val duration: String,
    val hasPlayed: Boolean,
    val progressSeconds: Int,
    val isBookmarked: Boolean,
    val isArchived: Boolean
)
