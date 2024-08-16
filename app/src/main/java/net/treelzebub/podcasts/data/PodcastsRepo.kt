package net.treelzebub.podcasts.data

import androidx.annotation.VisibleForTesting
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.ErrorHandler
import net.treelzebub.podcasts.util.Time
import timber.log.Timber
import javax.inject.Inject


class PodcastsRepo @Inject constructor(
    private val rssHandler: RssHandler,
    private val db: Database,
    private val queueStore: QueueStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /** RSS Feeds **/
    suspend fun fetchRssFeed(rssLink: String, onError: ErrorHandler) = withIoContext {
        try {
            Timber.d("Fetching RSS Feed: $rssLink")
            val pair = rssHandler.fetch(rssLink).podcastEpisodesPair(rssLink)
            upsertPodcast(pair)
        } catch (e: Exception) {
            Timber.e("Error parsing RSS Feed", e)
            onError(e)
        }
    }

    suspend fun parseRssFeed(rssLink: String, raw: String): Pair<Podcast, List<Episode>> =
        rssHandler.parse(raw).podcastEpisodesPair(rssLink)

    fun getAllRssLinks(): List<SubscriptionDto> {
        val rssLinksMapper = { id: String, rss_link: String -> SubscriptionDto(id, rss_link) }
        return db.podcastsQueries.get_all_rss_links(rssLinksMapper).executeAsList()
    }

    /** Podcasts **/
    suspend fun upsertPodcast(pair: Pair<Podcast, List<Episode>>) = withIoContext {
        db.transaction {
            db.podcastsQueries.upsert(pair.first)
            db.episodesQueries.upsert(pair.second, pair.first.image_url.orEmpty())
        }
    }

    suspend fun upsertPodcasts(podcasts: List<Podcast>) = withIoContext {
        db.transaction {
            podcasts.forEach { db.podcastsQueries.upsert(it) }
        }
    }

    suspend fun getPodcasts(): List<Podcast> = withIoContext {
        db.podcastsQueries.get_all().executeAsList()
    }

    suspend fun getPodcastUis(): Flow<List<PodcastUi>> = withIoContext {
        db.podcastsQueries.get_all(podcastMapper).asFlow().mapToList(ioDispatcher)
    }

    fun getPodcast(podcastId: String): Flow<PodcastUi?> {
        return db.podcastsQueries.get_by_id(podcastId, podcastMapper).asFlow().mapToOneOrNull(ioDispatcher)
    }

    // Cascading delete of episodes declared in episodes.sq
    suspend fun deletePodcastById(podcastId: String) = withIoContext {
        queueStore.removeByPodcastId(podcastId) {}
        db.podcastsQueries.delete(podcastId)
    }

    /** Episodes **/
    suspend fun getEpisodesList(podcastId: String): List<Episode> = withIoContext {
        db.episodesQueries.get_by_podcast_id(podcastId).executeAsList()
    }

    suspend fun getUnplayedEpisodes(podcastId: String): List<Episode> = withIoContext {
        db.episodesQueries.get_by_podcast_id_unplayed(podcastId).executeAsList()
    }

    fun getEpisodesFlow(podcastId: String, showPlayed: Boolean): Flow<List<EpisodeUi>> {
        return db.episodesQueries.let {
            if (showPlayed) it.get_by_podcast_id(podcastId, episodeMapper)
            else it.get_by_podcast_id_unplayed(podcastId, episodeMapper)
        }.asFlow().mapToList(ioDispatcher)
    }

    suspend fun getEpisodeById(id: String): EpisodeUi = withIoContext {
        db.episodesQueries.get_by_id(id, episodeMapper).executeAsOne()
    }

    fun getEpisodeFlowById(id: String): Flow<EpisodeUi> {
        return db.episodesQueries.get_by_id(id, episodeMapper).asFlow().mapToOne(ioDispatcher)
    }


    suspend fun updatePosition(id: String, millis: Long) = withIoContext {
        Timber.d("Persisting episode id: $id at position: $millis")
        db.episodesQueries.set_position_millis(millis, id)
    }

    suspend fun toggleIsBookmarked(episodeId: String) = withIoContext {
        db.episodesQueries.toggle_is_bookmarked(episodeId)
    }

    suspend fun toggleHasPlayed(episodeId: String) = withIoContext {
        db.episodesQueries.toggle_has_played(episodeId)
    }

    suspend fun toggleIsArchived(episodeId: String) = withIoContext {
        db.episodesQueries.toggle_is_archived(episodeId)
    }

    private suspend fun <T> withIoContext(block: suspend CoroutineScope.() -> T): T = withContext(ioDispatcher, block)

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
