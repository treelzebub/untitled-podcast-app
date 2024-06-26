package net.treelzebub.podcasts.data

import androidx.annotation.VisibleForTesting
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.ErrorHandler
import net.treelzebub.podcasts.util.Time
import net.treelzebub.podcasts.util.sanitizeHtml
import net.treelzebub.podcasts.util.sanitizeUrl
import timber.log.Timber
import javax.inject.Inject


/** TODO revisit this dependency graph, this class is outta hand **/
class PodcastsRepo @Inject constructor(
    private val rssHandler: RssHandler,
    private val db: Database,
    private val queueStore: QueueStore,
    private val ioDispatcher: CoroutineDispatcher
) {

    /** RSS Feeds **/
    suspend fun fetchRssFeed(rssLink: String, onError: ErrorHandler) {
        withIoContext {
            try {
                Timber.d("Fetching RSS Feed: $rssLink")
                val feed = rssHandler.fetch(rssLink)
                upsertPodcast(rssLink, feed)
            } catch (e: Exception) {
                Timber.e("Error parsing RSS Feed", e)
                onError(e)
            }
        }
    }

    suspend fun parseRssFeed(raw: String): RssChannel = rssHandler.parse(raw)

    suspend fun getAllRssLinks(): List<SubscriptionDto> {
        return withIoContext {
            db.podcastsQueries.get_all_rss_links().executeAsList()
                .map { SubscriptionDto(it.id, it.rss_link) }
        }
    }

    /** Podcasts **/
    suspend fun upsertPodcast(rssLink: String, channel: RssChannel) {
        withIoContext {
            db.transaction {
                val safeImage = channel.image?.url ?: channel.itunesChannelData?.image
                val latestEpisodeTimestamp = channel.items
                    .maxOfOrNull { Time.zonedEpochSeconds(it.pubDate) } ?: -1L
                with(channel) {
                    db.podcastsQueries.upsert(
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

    suspend fun getPodcastWithEpisodes(podcastId: String): Flow<Pair<PodcastUi, List<EpisodeUi>>?> {
        return withIoContext {
            db.podcastsQueries.transactionWithResult {
                db.podcastsQueries.get_by_id(podcastId, podcastMapper).asFlow().mapToOneOrNull(ioDispatcher)
                    .map { podcast ->
                        podcast?.let {
                            val episodes = db.episodesQueries.get_by_podcast_id(podcastId, episodeMapper).executeAsList()
                            it to episodes
                        }
                    }
            }
        }
    }

    suspend fun getPodcastsByLatestEpisode(): Flow<List<PodcastUi>> {
        return withIoContext {
            db.podcastsQueries.get_all(podcastMapper).asFlow().mapToList(ioDispatcher)
        }
    }

    suspend fun deletePodcastById(podcastId: String) = withIoContext {
        // Cascading delete of episodes declared in episodes.sq
        db.podcastsQueries.delete(podcastId)
        queueStore.removeByPodcastId(podcastId) { TODO() }
    }

    /** Episodes **/
    suspend fun getEpisodesByPodcastId(podcastId: String): Flow<List<EpisodeUi>> {
        return withIoContext {
            db.episodesQueries
                .get_by_podcast_id(podcastId, episodeMapper)
                .asFlow()
                .mapToList(ioDispatcher)
        }
    }

    suspend fun getEpisodeById(id: String): Flow<EpisodeUi> {
        return withIoContext {
            db.episodesQueries
                .get_by_id(id, episodeMapper)
                .asFlow()
                .mapToOne(ioDispatcher)
        }
    }

    suspend fun setIsBookmarked(episodeId: String, isBookmarked: Boolean) {
        withIoContext {
            db.episodesQueries.set_is_bookmarked(id = episodeId, is_bookmarked = isBookmarked)
        }
    }

    suspend fun setHasPlayed(episodeId: String, hasPlayed: Boolean) {
        withIoContext {
            db.episodesQueries.set_has_played(id = episodeId, has_played = hasPlayed)
        }
    }

    suspend fun setIsArchived(episodeId: String, isArchived: Boolean) {
        withIoContext {
            db.episodesQueries.set_is_archived(id = episodeId, is_archived = isArchived)
        }
    }


    /** Queue **/
    suspend fun addToQueue(episode: EpisodeUi, errorHandler: ErrorHandler) {
        withIoContext {
            queueStore.add(episode, errorHandler)
        }
    }

    suspend fun removeFromQueue(episodeId: String, errorHandler: ErrorHandler) {
        withIoContext {
            queueStore.remove(episodeId, errorHandler)
        }
    }

    suspend fun reorderQueue(from: Int, to: Int, errorHandler: ErrorHandler) {
        withIoContext {
            queueStore.reorder(from, to, errorHandler)
        }
    }


    private suspend fun <T> withIoContext(block: suspend () -> T): T = withContext(ioDispatcher) { block() }

    companion object {

        /** Mappers **/
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val podcastMapper: (
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

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val episodeMapper: (
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
}
