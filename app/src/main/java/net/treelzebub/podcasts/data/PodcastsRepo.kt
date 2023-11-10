package net.treelzebub.podcasts.data

import android.content.Context
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.orNow
import net.treelzebub.podcasts.util.sanitizeHtml
import net.treelzebub.podcasts.util.sanitizeUrl
import javax.inject.Inject

class PodcastsRepo @Inject constructor(
    private val db: Database
) {

    fun upsert(rssLink: String, channel: RssChannel) {
        db.transaction {
            with(channel) {
                db.podcastsQueries.upsert(
                    link!!.sanitizeUrl(),
                    title!!,
                    description?.sanitizeHtml(),
                    itunesChannelData?.owner?.email.orEmpty(),
                    image?.url,
                    lastBuildDate.orNow(),
                    rssLink.sanitizeUrl()
                )
            }
            channel.items.forEach {
                with(it) {
                    db.episodesQueries.upsert(
                        guid!!,
                        channel.link!!,
                        title!!.sanitizeHtml(),
                        description?.sanitizeHtml().orEmpty(),
                        pubDate,
                        link!!.sanitizeUrl(),
                        sourceUrl?.sanitizeUrl().orEmpty(),
                        image?.sanitizeUrl(),
                        itunesItemData?.duration
                    )
                }
            }
        }
    }

    fun getPodcastByLink(link: String): Flow<PodcastUi> {
        return db.podcastsQueries
            .get_podcast_by_link(link, podcastMapper)
            .asFlow()
            .mapToOne(Dispatchers.IO)
    }

    fun getAllPodcasts(): Flow<List<PodcastUi>> {
        return db.podcastsQueries
            .get_all_podcasts(podcastMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

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
        link: String,
        title: String,
        description: String?,
        email: String?,
        image_url: String?,
        last_fetched: String,
        rss_link: String
    ) -> PodcastUi = { link, title, description,
                       email, image_url, last_fetched, rss_link ->
        PodcastUi(
            link, title, description.orEmpty(), email.orEmpty(), image_url.orEmpty(),
            last_fetched, rss_link
        )
    }

    private val episodeMapper: (
        id: String,
        channel_id: String,
        title: String,
        description: String?,
        date: String?,
        link: String,
        streaming_link: String,
        image_url: String?,
        duration: String?
    ) -> EpisodeUi = { id, channel_id, title, description, date, link,
                       streaming_link, image_url, duration ->
        EpisodeUi(
            id, channel_id, title, description.orEmpty(), date.orEmpty(), link,
            streaming_link, image_url.orEmpty(), duration.orEmpty()
        )
    }

    // TODO rm
    suspend fun test(context: Context) {
        val rss = context.assets.open("test.rss").bufferedReader().use { it.readText() }
        val channel = RssParser().parseRss(rss)
        db.transaction {
            with(channel) {
                db.podcastsQueries.upsert(
                    link!!.sanitizeUrl(),
                    title!!,
                    description?.sanitizeHtml(),
                    itunesChannelData?.owner?.email.orEmpty(),
                    image?.url,
                    lastBuildDate.orNow(),
                    link!!.sanitizeUrl()
                )
            }
            channel.items.forEach {
                with(it) {
                    db.episodesQueries.upsert(
                        guid!!,
                        channel.link!!,
                        title!!.sanitizeHtml(),
                        description?.sanitizeHtml().orEmpty(),
                        pubDate,
                        link!!.sanitizeUrl(),
                        sourceUrl?.sanitizeUrl().orEmpty(),
                        image?.sanitizeUrl(),
                        itunesItemData?.duration
                    )
                }
            }
        }
    }
}