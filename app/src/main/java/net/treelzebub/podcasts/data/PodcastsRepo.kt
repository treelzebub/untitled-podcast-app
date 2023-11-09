package net.treelzebub.podcasts.data

import android.text.Html
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.ui.models.PodcastUi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PodcastsRepo @Inject constructor(
    private val db: Database
) {

    fun insert(rssLink: String, channel: RssChannel) {
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

    fun getPodcasts(): Flow<List<PodcastUi>> {
        return db.podcastsQueries.get_all_podcasts { link, title, description, email, image_url, last_fetched, rss_link ->
            PodcastUi(link, title, description.orEmpty(), email.orEmpty(), image_url.orEmpty(), last_fetched, rss_link)
        }.asFlow().mapToList(Dispatchers.IO)
    }

    private fun String.sanitizeUrl(): String = replace("&amp;", "&")
    private fun String?.sanitizeHtml(): String = this?.let { Html.fromHtml(this).toString() }.orEmpty()
    private fun String?.orNow(): String = this ?: "TODO()"

    private fun String.formatDate(): String {
        val localDateTime = LocalDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME)
        val displayFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        return localDateTime.format(displayFormat)
    }
}