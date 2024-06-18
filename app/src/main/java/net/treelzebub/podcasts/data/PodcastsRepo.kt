package net.treelzebub.podcasts.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.Time
import net.treelzebub.podcasts.util.sanitizeHtml
import net.treelzebub.podcasts.util.sanitizeUrl
import timber.log.Timber
import javax.inject.Inject


class PodcastsRepo @Inject constructor(
    private val rssHandler: RssHandler,
    private val db: Database,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun fetchRssFeed(rssLink: String, onError: (Exception) -> Unit) {
        try {
            Timber.d("Fetching RSS Feed: $rssLink")
            val feed = rssHandler.fetch(rssLink)
            insertOrReplacePodcast(rssLink, feed)
        } catch (e: Exception) {
            Timber.e("Error parsing RSS Feed", e)
            onError(e)
        }
    }

    suspend fun parseRssFeed(raw: String): RssChannel = rssHandler.parse(raw)

    /** Podcasts **/
    suspend fun insertOrReplacePodcast(rssLink: String, channel: RssChannel) {
        withContext(ioDispatcher) {
            db.transaction {
                val safeImage = channel.image?.url ?: channel.itunesChannelData?.image
                val latestEpisodeTimestamp = channel.items
                    .maxBy { Time.zonedEpochSeconds(it.pubDate) }
                    .let { Time.zonedEpochSeconds(it.pubDate) }
                with(channel) {
                    db.podcastsQueries.insert_or_replace(
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
                channel.items.forEach {
                    with(it) {
                        db.episodesQueries.upsert(
                            id = guid!!,
                            podcast_id = channel.link!!,
                            podcast_title = channel.title!!,
                            title = title.sanitizeHtml() ?: "[No Title]",
                            description = description?.sanitizeHtml() ?: "[No Description]",
                            date = Time.zonedEpochSeconds(pubDate),
                            link = link?.sanitizeUrl().orEmpty(),
                            streaming_link = audio.orEmpty(),
                            image_url = image?.sanitizeUrl() ?: safeImage,
                            duration = itunesItemData?.duration,
                        )
                    }
                }
            }
        }
    }

    suspend fun insertOrReplacePodcast(podcastUi: PodcastUi) {
        withContext(ioDispatcher) {
            db.podcastsQueries.insert_or_replace(podcastUi)
        }
    }

    suspend fun getPodcastById(id: String): Flow<PodcastUi> {
        return withContext(ioDispatcher) {
            db.podcastsQueries.get_by_id(id, podcastMapper).executeAsList().asFlow()
        }
    }

    suspend fun getPodcastByLink(rssLink: String): Flow<PodcastUi?> {
        return withContext(ioDispatcher) {
            db.podcastsQueries
                .get_by_link(rssLink, podcastMapper)
                .asFlow()
                .mapToOneOrNull(ioDispatcher)
        }
    }

    suspend fun getPodcastPair(podcastId: String): Flow<Pair<PodcastUi, List<EpisodeUi>>> {
        return withContext(ioDispatcher) {
            db.episodesQueries.transactionWithResult {
                db.podcastsQueries.get_by_id(podcastId, podcastMapper).asFlow().mapToOne(ioDispatcher)
                    .map { pod ->
                        val episodes = db.episodesQueries.get_by_podcast_id(podcastId, episodeMapper).executeAsList()
                        pod to episodes
                    }
            }
        }
    }

    suspend fun getPodcastMap(): Flow<Map<PodcastUi, List<EpisodeUi>>> {
        return withContext(ioDispatcher) {
            db.episodesQueries.transactionWithResult {
                db.podcastsQueries.get_all(podcastMapper).asFlow()
                    .mapToList(ioDispatcher)
                    .map { podcasts ->
                        val map = podcasts.associateBy { it.id }
                        db.episodesQueries.get_all(episodeMapper).executeAsList()
                            .groupBy { it.podcastId }.entries.associate { entry ->
                                map[entry.key]!! to entry.value.sortedByDescending { it.sortDate }
                            }
                    }
            }
        }
    }

    suspend fun getPodcastsByLatestEpisode(): Flow<List<PodcastUi>> {
        return withContext(ioDispatcher) {
            db.podcastsQueries.get_all(podcastMapper).asFlow().mapToList(ioDispatcher)
        }
    }

    suspend fun getAllRssLinks(): List<SubscriptionDto> {
        return withContext(ioDispatcher) {
             db.podcastsQueries
                .get_all_rss_links()
                .executeAsList()
                .map { SubscriptionDto(it.id, it.rss_link) }
        }
    }

    suspend fun deletePodcastById(link: String) = withContext(ioDispatcher) {
        db.podcastsQueries.delete(link)
    }

    /** Episodes **/
    private suspend fun getAllEpisodes(): List<EpisodeUi> {
        return withContext(ioDispatcher) {
            db.episodesQueries.get_all(episodeMapper).executeAsList()
        }
    }

    suspend fun getEpisodesByPodcastId(podcastId: String): Flow<List<EpisodeUi>> {
        return withContext(ioDispatcher) {
            db.episodesQueries
                .get_by_podcast_id(podcastId, episodeMapper)
                .asFlow()
                .mapToList(ioDispatcher)
        }
    }

    suspend fun getEpisodeById(id: String): Flow<EpisodeUi> {
        return withContext(ioDispatcher) {
            db.episodesQueries
                .get_by_id(id, episodeMapper)
                .asFlow()
                .mapToOne(ioDispatcher)
        }
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
        last_local_update: Long,
        latestEpisodeTimestamp: Long
    ) -> PodcastUi = { id, link, title, description,
                       email, image_url, last_build_date,
                       rss_link, lastLocalUpdate, latestEpisodeTimestamp ->
        PodcastUi(
            id, link, title, description.orEmpty(), email.orEmpty(), image_url.orEmpty(),
            Time.displayFormat(last_build_date), rss_link, lastLocalUpdate, latestEpisodeTimestamp
        )
    }

    private val episodeMapper: (
        id: String,
        podcast_id: String,
        podcast_title: String,
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
    ) -> EpisodeUi = { id, podcast_id, podcast_title, title,
                       description, date, link, streaming_link,
                       image_url, duration, has_played, progress_seconds,
                       is_bookmarked, is_archived ->
        EpisodeUi(
            id = id,
            podcastId = podcast_id,
            podcastTitle = podcast_title,
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
