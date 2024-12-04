package net.treelzebub.podcasts.data

import com.prof18.rssparser.model.RssChannel
import com.prof18.rssparser.model.RssItem
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.util.Strings
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
    duration = Strings.formatDuration(itunesItemData?.duration),
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
        id = rssLink,
        link = link!!   ,
        title = title!!,
        description = description?.sanitizeHtml() ?: itunesChannelData?.subtitle.sanitizeHtml()
            .orEmpty(),
        email = itunesChannelData?.owner?.email ?: items.firstOrNull()?.author.orEmpty(),
        image_url = safeImage,
        last_build_date = Time.zonedEpochSeconds(lastBuildDate),
        rss_link = rssLink,
        last_local_update = Time.nowSeconds(),
        latest_episode_timestamp = latestEpisodeTimestamp
    )
}

fun RssChannel.podcastEpisodesPair(rssLink: String): Pair<Podcast, List<Episode>> {
    val podcast = podcast(itunesChannelData?.newsFeedUrl ?: rssLink)
    val episodes = items.map { it.episode(podcast.id) }
    return podcast to episodes
}
