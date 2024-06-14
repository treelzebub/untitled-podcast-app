package net.treelzebub.podcasts.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.Log
import net.treelzebub.podcasts.util.Time
import net.treelzebub.podcasts.util.sanitizeHtml
import net.treelzebub.podcasts.util.sanitizeUrl
import javax.inject.Inject


class PodcastsRepo @Inject constructor(
    private val rssHandler: RssHandler,
    private val db: Database
) {

    suspend fun fetchRssFeed(url: String, onError: (Exception) -> Unit) {
        try {
            Log.d("PodcastRepo", "Fetching RSS Feed: $url")
            val feed = rssHandler.fetch(url)
            upsert(url, feed)
        } catch (e: Exception) {
            Log.e("PodcastRepo", "Error parsing RSS Feed", e)
            onError(e)
        }
    }

    suspend fun parseRssFeed(raw: String): RssChannel = rssHandler.parse(raw)

    fun upsert(url: String, channel: RssChannel) {
        db.transaction {
            val safeImage = channel.image?.url ?: channel.itunesChannelData?.image
            with(channel) {
                db.podcastsQueries.upsert(
                    id = url, // Public link to Podcast will be unique, so it's our ID.
                    link = link!!,
                    title = title!!,
                    description = description?.sanitizeHtml() ?: itunesChannelData?.subtitle.sanitizeHtml().orEmpty(),
                    email = itunesChannelData?.owner?.email.orEmpty(),
                    image_url = safeImage,
                    last_build_date = Time.zonedEpochMillis(lastBuildDate),
                    rss_link = url,
                    last_local_update = Time.nowEpochMillis()
                )
            }
            channel.items.forEach {
                with(it) {
                    db.episodesQueries.upsert(
                        id = guid!!,
                        channel_id = url,
                        channel_title = channel.title!!,
                        title = title.sanitizeHtml() ?: "[No Title]",
                        description = description?.sanitizeHtml() ?: "[No Description]",
                        date = Time.zonedEpochMillis(pubDate),
                        link = link?.sanitizeUrl().orEmpty(),
                        streaming_link = audio.orEmpty(),
                        image_url = image?.sanitizeUrl() ?: safeImage,
                        duration = itunesItemData?.duration,
                    )
                }
            }
        }
    }

    fun getPodcastByLink(rssLink: String): Flow<PodcastUi?> {
        return db.podcastsQueries
            .get_podcast_by_link(rssLink, podcastMapper)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    }

    fun getAllAsFlow(): Flow<List<PodcastUi>> {
        return db.podcastsQueries
            .get_all_podcasts(podcastMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    fun getAllAsList(): List<PodcastUi> {
        return db.podcastsQueries
            .get_all_podcasts(podcastMapper)
            .executeAsList()
    }

    fun getAllRssLinks(): List<SubscriptionDto> {
        return db.podcastsQueries
            .get_all_rss_links()
            .executeAsList()
            .map { SubscriptionDto(it.id, it.rss_link) }
    }

    fun deletePodcastById(link: String) = db.podcastsQueries.delete(link)

    fun getEpisodesByChannelLink(link: String): Flow<List<EpisodeUi>> {
        return db.episodesQueries
            .get_by_channel_id(link, episodeMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    fun getEpisodeById(id: String): Flow<EpisodeUi> {
        return db.episodesQueries
            .get_by_id(id, episodeMapper)
            .asFlow()
            .mapToOne(Dispatchers.IO)
    }


    private val podcastMapper: (
        id: String,
        link: String,
        title: String,
        description: String?,
        email: String?,
        image_url: String?,
        last_build_date: Long,
        rss_link: String,
        lastLocalUpdate: Long
    ) -> PodcastUi = { id, link, title, description,
                       email, image_url, last_build_date,
                       rss_link, lastLocalUpdate ->
        PodcastUi(
            id, link, title, description.orEmpty(), email.orEmpty(), image_url.orEmpty(),
            Time.displayFormat(last_build_date), rss_link, lastLocalUpdate
        )
    }

    private val episodeMapper: (
        id: String,
        channel_id: String,
        channel_title: String,
        title: String,
        description: String?,
        date: Long,
        link: String,
        streaming_link: String,
        image_url: String?,
        duration: String?,
        has_played: Boolean,
        progress_seconds: Long,
        is_bookmarked: Boolean,
        is_archived: Boolean
    ) -> EpisodeUi = { id, channel_id, channel_title, title,
                       description, date, link, streaming_link,
                       image_url, duration, has_played, progress_seconds,
                       is_bookmarked, is_archived ->
        EpisodeUi(
            id = id,
            channelId = channel_id,
            channelTitle = channel_title,
            title = title,
            description = description.orEmpty(),
            date = Time.displayFormat(date),
            link = link,
            streamingLink = streaming_link,
            imageUrl = image_url.orEmpty(),
            duration = duration.orEmpty(),
            hasPlayed = has_played,
            progressSeconds = progress_seconds.toInt(),
            isBookmarked = is_bookmarked,
            isArchived = is_archived
        )
    }
}
