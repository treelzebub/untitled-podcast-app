package net.treelzebub.podcasts.data

import androidx.core.text.isDigitsOnly
import com.prof18.rssparser.model.RssChannel
import com.prof18.rssparser.model.RssItem
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.util.Time
import net.treelzebub.podcasts.util.sanitizeHtml
import net.treelzebub.podcasts.util.sanitizeUrl


fun RssItem.episode(podcastId: String) = Episode(
    id = guid!!,
    podcast_id = podcastId,
    podcast_title = this.title!!,
    title = title.sanitizeHtml() ?: "[No Title]",
    description = description?.sanitizeHtml() ?: "[No Description]",
    date = Time.zonedEpochSeconds(pubDate),
    link = link?.sanitizeUrl().orEmpty(),
    streaming_link = audio.orEmpty(),
    local_file_uri = null,
    image_url = image?.sanitizeUrl() ?: this.image,
    duration = formatDuration(itunesItemData?.duration),

    // Ignored, TODO separate model and query that omits these?
    has_played = false,
    position_millis = 0L,
    is_bookmarked = false,
    is_archived = false
)

fun RssChannel.podcast(rssLink: String): Podcast {
    val safeImage = this.image?.url?.sanitizeUrl() ?: this.image?.url ?: this.itunesChannelData?.image.orEmpty()
    val latestEpisodeTimestamp = this.items
        .maxOfOrNull { Time.zonedEpochSeconds(it.pubDate) } ?: -1L
    return Podcast(
        id = link!!, // Public link to Podcast will be unique, so it's our ID.
        link = link!!,
        title = title!!,
        description = description?.sanitizeHtml() ?: itunesChannelData?.subtitle.sanitizeHtml()
            .orEmpty(),
        email = itunesChannelData?.owner?.email.orEmpty(),
        image_url = safeImage,
        last_build_date = Time.zonedEpochSeconds(lastBuildDate),
        rss_link = rssLink,
        last_local_update = Time.nowSeconds(),
        latest_episode_timestamp = latestEpisodeTimestamp
    )
}

fun RssChannel.podcastEpisodesPair(rssLink: String): Pair<Podcast, List<Episode>> {
    val podcast = podcast(rssLink)
    val episodes = items.map { it.episode(podcast.id) }
    return podcast to episodes
}

private fun formatDuration(seconds: String?): String {
    val timecodePattern = Regex("""^(?:[01]\d|2[0-3]):[0-5]\d(?::[0-5]\d)?$""")

    if (seconds?.matches(timecodePattern) == true) {
        val parts = seconds.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()

        return if (hours == 0) "${minutes}m" else "${hours}h ${minutes}m"
    }

    if (seconds?.isDigitsOnly() == true) {
        val long = seconds.toLong()
        val mins = long / 60
        val hours = mins / 60
        val secs = mins % 60
        return "" +
            if (hours > 0) "${hours}h" else "" +
                if (mins > 0) "${mins}m" else "" +
                    "${secs}s"
    }

    return ""
}