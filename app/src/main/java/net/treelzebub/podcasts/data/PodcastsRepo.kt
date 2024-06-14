package net.treelzebub.podcasts.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    private val db: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun fetchRssFeed(url: String, onError: (Exception) -> Unit) {
        try {
            Log.d("PodcastRepo", "Fetching RSS Feed: $url")
            val feed = rssHandler.fetch(url)
            insertOrReplacePodcast(url, feed)
        } catch (e: Exception) {
            Log.e("PodcastRepo", "Error parsing RSS Feed", e)
            onError(e)
        }
    }

    suspend fun parseRssFeed(raw: String): RssChannel = rssHandler.parse(raw)

    /** Podcasts **/
    fun insertOrReplacePodcast(url: String, channel: RssChannel) {
        db.transaction {
            val safeImage = channel.image?.url ?: channel.itunesChannelData?.image
            with(channel) {
                db.podcastsQueries.insert_or_replace(
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

    fun insertOrReplacePodcast(podcastUi: PodcastUi) {
        db.podcastsQueries.insert_or_replace(podcastUi)
    }

    fun getPodcastByLink(rssLink: String): Flow<PodcastUi?> {
        return db.podcastsQueries
            .get_by_link(rssLink, podcastMapper)
            .asFlow()
            .mapToOneOrNull(dispatcher)
    }

    fun getAllPodcastsFlow(): Flow<List<PodcastUi>> {
        return db.podcastsQueries
            .get_all(podcastMapper)
            .asFlow()
            .mapToList(dispatcher)
    }

    fun getAllPodcastsByLatestEpisode(): Flow<Map<PodcastUi, List<EpisodeUi>>> {
        val podcasts = db.podcastsQueries.get_all(podcastMapper).executeAsList()
        return db.episodesQueries.get_all(episodeMapper)
            .asFlow()
            .mapToList(dispatcher)
            .map { episodes ->
                episodes.groupBy {
                    it.channelId
                }.entries.associate { (podcastId, episodes) ->
                    podcasts.find { it.id == podcastId }!! to episodes.sortedByDescending { it.sortDate }
                }
            }
    }

    fun getAllRssLinks(): List<SubscriptionDto> {
        return db.podcastsQueries
            .get_all_rss_links()
            .executeAsList()
            .map { SubscriptionDto(it.id, it.rss_link) }
    }

    fun deletePodcastById(link: String) = db.podcastsQueries.delete(link)

    /** Episodes **/
    private fun getAllEpisodes(): List<EpisodeUi> {
        return db.episodesQueries.get_all(episodeMapper).executeAsList()
    }

    fun getEpisodesByChannelLink(link: String): Flow<List<EpisodeUi>> {
        return db.episodesQueries
            .get_by_channel_id(link, episodeMapper)
            .asFlow()
            .mapToList(dispatcher)
    }

    fun getEpisodeById(id: String): Flow<EpisodeUi> {
        return db.episodesQueries
            .get_by_id(id, episodeMapper)
            .asFlow()
            .mapToOne(dispatcher)
    }

    /** Mappers **/
    private val podcastMapper: (
        id: String,
        link: String,
        title: String,
        description: String?,
        email: String?,
        image_url: String?,
        last_build_date: Long,
        rss_link: String,
        last_local_update: Long
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
            displayDate = Time.displayFormat(date),
            sortDate = date,
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
