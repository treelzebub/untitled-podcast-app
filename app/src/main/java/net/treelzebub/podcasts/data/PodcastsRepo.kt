package net.treelzebub.podcasts.data

import android.util.Log
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.Time
import net.treelzebub.podcasts.util.orNow
import net.treelzebub.podcasts.util.sanitizeHtml
import net.treelzebub.podcasts.util.sanitizeUrl
import javax.inject.Inject


class PodcastsRepo @Inject constructor(
    private val rssHandler: RssHandler,
    private val db: Database
) {

    suspend fun fetchRssFeed(url: String, onError: (Exception) -> Unit) {
        try {
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
                val link = link!!.sanitizeUrl()!!
                db.podcastsQueries.upsert(
                    link, // Public link to Podcast will be unique, so it's our ID.
                    link,
                    title!!,
                    description?.sanitizeHtml() ?: itunesChannelData?.subtitle.sanitizeHtml().orEmpty(),
                    itunesChannelData?.owner?.email.orEmpty(),
                    safeImage,
                    lastBuildDate.orNow(),
                    itunesChannelData?.newsFeedUrl ?: url
                )
            }
            channel.items.forEach {
                with(it) {
                    db.episodesQueries.upsert(
                        guid!!,
                        channel.link!!,
                        channel.title!!,
                        title.sanitizeHtml()!!,
                        description?.sanitizeHtml().orEmpty(),
                        pubDate,
                        link?.sanitizeUrl().orEmpty(),
                        audio.orEmpty(),
                        image?.sanitizeUrl() ?: safeImage,
                        itunesItemData?.duration
                    )
                }
            }
        }
    }

    fun getPodcastByLink(link: String): Flow<PodcastUi?> {
        return db.podcastsQueries
            .get_podcast_by_link(link, podcastMapper)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    }

    fun getAllAsFlow(): Flow<List<PodcastUi>> {
        return db.podcastsQueries
            .get_all_podcasts(podcastMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun getAllAsList(): List<PodcastUi> {
        return db.podcastsQueries
            .get_all_podcasts(podcastMapper)
            .executeAsList()
    }

    fun deletePodcastById(link: String) = db.podcastsQueries.delete(link)

    fun getEpisodesByChannelLink(link: String): Flow<List<EpisodeUi>> {
        return db.episodesQueries
            .get_episodes_by_channel_id(link, episodeMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    fun getEpisodeById(id: String): Flow<EpisodeUi> {
        return db.episodesQueries
            .get_episode_by_id(id, episodeMapper)
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
        last_fetched: String,
        rss_link: String
    ) -> PodcastUi = { id, link, title, description,
                       email, image_url, last_fetched, rss_link ->
        PodcastUi(
            id, link, title, description.orEmpty(), email.orEmpty(), image_url.orEmpty(),
            Time.displayFormat(last_fetched), rss_link
        )
    }

    private val episodeMapper: (
        id: String,
        channel_id: String,
        channel_title: String,
        title: String,
        description: String?,
        date: String?,
        link: String,
        streaming_link: String,
        image_url: String?,
        duration: String?
    ) -> EpisodeUi = { id, channel_id, channel_title, title, description, date, link,
                       streaming_link, image_url, duration ->
        EpisodeUi(
            id, channel_id, channel_title, title, description.orEmpty(), Time.displayFormat(date), link,
            streaming_link, image_url.orEmpty(), duration.orEmpty()
        )
    }
}
