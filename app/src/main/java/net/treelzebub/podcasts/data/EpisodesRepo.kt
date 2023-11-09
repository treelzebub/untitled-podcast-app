package net.treelzebub.podcasts.data

import android.content.Context
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.util.orNow
import net.treelzebub.podcasts.util.sanitizeHtml
import net.treelzebub.podcasts.util.sanitizeUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodesRepo @Inject constructor(
    private val db: Database,
) {
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

    fun getEpisodesByChannelId(channelId: String): Flow<List<EpisodeUi>> {
        return db.episodesQueries.get_episodes_by_channel_id(channelId) {
                id, channel_id, title, description, date, link, streaming_link, image_url, duration ->
            EpisodeUi(
                id, channel_id, title, description.orEmpty(), date.orEmpty(), link,
                streaming_link, image_url.orEmpty(), duration.orEmpty()
            )
        }.asFlow().mapToList(Dispatchers.IO)
    }
}