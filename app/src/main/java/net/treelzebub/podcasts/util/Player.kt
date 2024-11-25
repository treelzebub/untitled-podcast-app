package net.treelzebub.podcasts.util

import androidx.media3.common.MediaItem
import androidx.media3.common.Player


val Player.mediaItems: List<MediaItem>
    get() = (0 until mediaItemCount).map { getMediaItemAt(it) }

fun List<MediaItem>.indexOf(id: String) = map { it.mediaId }.indexOf(id)
