package net.treelzebub.podcasts.data

import androidx.annotation.VisibleForTesting
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun cancelScope() = scope.cancel()

    /** RSS Feeds **/
    fun fetchRssFeed(rssLink: String, onError: ErrorHandler) {
        scope.launch {
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
        return await {
            db.podcastsQueries.get_all_rss_links().executeAsList()
                .map { SubscriptionDto(it.id, it.rss_link) }
        }
    }

    /** Podcasts **/
    fun upsertPodcast(rssLink: String, channel: RssChannel) {
        scope.launch {
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
                            local_file_uri = null,
                            image_url = image?.sanitizeUrl() ?: safeImage,
                            duration = itunesItemData?.duration,
                            has_played = false,
                            progress_millis = 0L,
                            is_bookmarked = false,
                            is_archived = false
                        )
                    }
                }
            }
        }
    }

    suspend fun getPodcastWithEpisodes(podcastId: String): Flow<Pair<PodcastUi, List<EpisodeUi>>?> {
        return await {
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
        return await {
            db.podcastsQueries.get_all(podcastMapper).asFlow().mapToList(ioDispatcher)
        }
    }

    // Cascading delete of episodes declared in episodes.sq
    suspend fun deletePodcastById(podcastId: String) = await {
        queueStore.removeByPodcastId(podcastId) {}
        db.podcastsQueries.delete(podcastId)
    }

    /** Episodes **/
    suspend fun getEpisodesByPodcastId(podcastId: String): Flow<List<EpisodeUi>> {
        return await {
            db.episodesQueries
                .get_by_podcast_id(podcastId, episodeMapper)
                .asFlow()
                .mapToList(ioDispatcher)
        }
    }

    suspend fun getEpisodeById(id: String): EpisodeUi {
        return await {
            db.episodesQueries.get_by_id(id, episodeMapper).executeAsOneOrNull()
                ?: throw IllegalArgumentException("Episode is not in database!")
        }
    }

    fun updatePosition(id: String, millis: Long) {
        Timber.d("Persisting episode id: $id at position: $millis")
        scope.launch {
            db.episodesQueries.set_position_millis(millis, id)
        }
    }

    fun setIsBookmarked(episodeId: String, isBookmarked: Boolean) {
        scope.launch {
            db.episodesQueries.set_is_bookmarked(id = episodeId, is_bookmarked = isBookmarked)
        }
    }

    fun setHasPlayed(episodeId: String, hasPlayed: Boolean) {
        scope.launch {
            db.episodesQueries.set_has_played(id = episodeId, has_played = hasPlayed)
        }
    }

    fun setIsArchived(episodeId: String, isArchived: Boolean) {
        scope.launch {
            db.episodesQueries.set_is_archived(id = episodeId, is_archived = isArchived)
        }
    }


    /** Queue **/
    fun addToQueue(episode: EpisodeUi, errorHandler: ErrorHandler) {
        scope.launch {
            queueStore.add(episode, errorHandler)
        }
    }

    suspend fun removeFromQueue(episodeId: String, errorHandler: ErrorHandler) {
        scope.launch {
            queueStore.remove(episodeId, errorHandler)
        }
    }

    suspend fun reorderQueue(from: Int, to: Int, errorHandler: ErrorHandler) {
        scope.launch {
            queueStore.reorder(from, to, errorHandler)
        }
    }

    private suspend fun <T> await(block: suspend () -> T): T = scope.async { block() }.await()

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
            local_file_uri: String?,
            image_url: String?,
            duration: String?,
            has_played: Boolean,
            progress_seconds: Long,
            is_bookmarked: Boolean,
            is_archived: Boolean
        ) -> EpisodeUi = { id, podcast_id, podcast_title, title,
                           description, date, link, streaming_link, local_file_uri,
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
                localFileUri = local_file_uri,
                imageUrl = image_url.orEmpty(),
                duration = duration.orEmpty(),
                hasPlayed = has_played,
                positionMillis = progress_seconds,
                isBookmarked = is_bookmarked,
                isArchived = is_archived
            )
        }
    }
}
