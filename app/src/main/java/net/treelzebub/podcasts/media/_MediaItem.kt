package net.treelzebub.podcasts.media

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import net.treelzebub.podcasts.ui.models.EpisodeUi

fun EpisodeUi.toMediaItem(): MediaItem {
    val mediaMetaData = MediaMetadata.Builder()
        .setArtworkUri(Uri.parse(imageUrl))
        .setTitle(title)
        .setAlbumArtist(podcastTitle)
        .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST)
        .build()
    val trackUri = Uri.parse(streamingLink)
    val builder = MediaItem.Builder()
        .setUri(trackUri)
        .setMediaId(id)
        .setMediaMetadata(mediaMetaData)
        .setMimeType("audio/mpeg")
    if (positionMillis > 0) {
        builder.setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder().setStartPositionMs(positionMillis).build()
        )
    }
    return builder.build()
}