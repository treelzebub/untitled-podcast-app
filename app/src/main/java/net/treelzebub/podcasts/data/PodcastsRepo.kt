package net.treelzebub.podcasts.data

import androidx.annotation.VisibleForTesting
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.ErrorHandler
import net.treelzebub.podcasts.util.Time
import timber.log.Timber
import javax.inject.Inject


// TODO revisit this dependency graph, this class is outta hand
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
                val pair = rssHandler.fetch(rssLink).podcastEpisodesPair(rssLink)
                upsertPodcast(pair)
            } catch (e: Exception) {
                Timber.e("Error parsing RSS Feed", e)
                onError(e)
            }
        }
    }

    suspend fun parseRssFeed(rssLink: String, raw: String): Pair<Podcast, List<Episode>> = rssHandler.parse(raw).podcastEpisodesPair(rssLink)

    fun getAllRssLinks(): List<SubscriptionDto> {
        val rssLinksMapper = { id: String, rss_link: String -> SubscriptionDto(id, rss_link) }
        return db.podcastsQueries.get_all_rss_links(rssLinksMapper).executeAsList()
    }

    /** Podcasts **/
    fun upsertPodcast(pair: Pair<Podcast, List<Episode>>) {
        scope.launch {
            db.transaction {
                val podcast = pair.first
                val episodes = pair.second
                with(podcast) {
                    db.podcastsQueries.upsert(
                        id = id,
                        link = link,
                        title = title,
                        description = description,
                        email = email,
                        image_url = image_url,
                        last_build_date = last_build_date,
                        rss_link = rss_link,
                        last_local_update = last_local_update,
                        latest_episode_timestamp = latest_episode_timestamp,
                    )
                }
                episodes.forEach {
                    with(it) {
                        db.episodesQueries.upsert(
                            id = id,
                            podcast_id = podcast_id,
                            podcast_title = podcast_title,
                            title = title,
                            description = description,
                            date = date,
                            link = link,
                            streaming_link = streaming_link,
                            local_file_uri = local_file_uri,
                            image_url = image_url,
                            duration = duration,
                            has_played = has_played,
                            position_millis = position_millis,
                            is_bookmarked = is_bookmarked,
                            is_archived = is_archived
                        )
                    }
                }
            }
        }
    }

    fun getPodcasts(): List<Podcast> {
        return db.podcastsQueries.get_all().executeAsList()
    }

    fun getPodcastUis(): Flow<List<PodcastUi>> {
        return db.podcastsQueries.get_all(podcastMapper).asFlow().mapToList(ioDispatcher)
    }

    fun getPodcast(podcastId: String): Flow<PodcastUi?> {
        return db.podcastsQueries.get_by_id(podcastId, podcastMapper).asFlow().mapToOneOrNull(ioDispatcher)
    }

    // Cascading delete of episodes declared in episodes.sq
    suspend fun deletePodcastById(podcastId: String) {
        queueStore.removeByPodcastId(podcastId) {}
        db.podcastsQueries.delete(podcastId)
    }

    /** Episodes **/
    fun getAllEpisodesList(podcastId: String): List<Episode> {
        return db.episodesQueries.get_by_podcast_id(podcastId).executeAsList()
    }

    fun getEpisodesFlow(podcastId: String, showPlayed: Boolean): Flow<List<EpisodeUi>> {
        return db.episodesQueries.let {
            if (showPlayed) it.get_by_podcast_id(podcastId, episodeMapper)
            else it.get_by_podcast_id_unplayed(podcastId, episodeMapper)
        }.asFlow().mapToList(ioDispatcher)
    }

    fun getEpisodeById(id: String): EpisodeUi {
        return db.episodesQueries.get_by_id(id, episodeMapper).executeAsOneOrNull()
            ?: throw IllegalArgumentException("Episode is not in database!")
    }

    fun getEpisodeFlowById(id: String): Flow<EpisodeUi?> {
        return db.episodesQueries.get_by_id(id, episodeMapper).asFlow().mapToOneOrNull(ioDispatcher)
    }


    fun updatePosition(id: String, millis: Long) {
        Timber.d("Persisting episode id: $id at position: $millis")
        scope.launch {
            db.episodesQueries.set_position_millis(millis, id)
        }
    }

    fun toggleIsBookmarked(episodeId: String) {
        scope.launch {
            db.episodesQueries.toggle_is_bookmarked(episodeId)
        }
    }

    fun toggleHasPlayed(episodeId: String) {
        scope.launch {
            db.episodesQueries.toggle_has_played(episodeId)
        }
    }

    fun toggleIsArchived(episodeId: String) {
        scope.launch {
            db.episodesQueries.toggle_is_archived(episodeId)
        }
    }

    /** Queue **/
    fun addToQueue(id: String, errorHandler: ErrorHandler) {
        scope.launch {
            queueStore.add(getEpisodeById(id), errorHandler)
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
