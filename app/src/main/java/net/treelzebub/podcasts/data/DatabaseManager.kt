package net.treelzebub.podcasts.data

import android.content.Context
import android.text.Html
import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.prof18.rssparser.model.RssChannel
import dagger.hilt.android.qualifiers.ApplicationContext
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseManager @Inject constructor(
    @ApplicationContext context: Context,
    driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "podcasts.db")
) {
    private val db: Database

    init {
        db = Database(driver)
    }

    // TODO null safety
    fun insert(rssLink: String, channel: RssChannel) {
        db.transaction {
            with(channel) {
                db.podcastsQueries.upsert(
                    link!!.sanitizeUrl(), title!!, description, itunesChannelData?.owner?.email,
                    image?.url, lastBuildDate.orNow(), rssLink.sanitizeUrl()
                )
            }
            channel.items.forEach {
                with(it) {
                    db.episodesQueries.upsert(
                        guid!!, channel.link!!, title!!, description, pubDate, link!!.sanitizeUrl(), sourceUrl!!.sanitizeUrl(), image?.sanitizeUrl(), itunesItemData?.duration
                    )
                }
            }
        }
    }

    fun getEpisodesFromChannel(channelId: String): List<Episode> {
        return db.episodesQueries.get_episodes_by_channel_id(channelId).executeAsList()
    }

    fun getAllPodcasts(): List<Podcast> {
        return db.podcastsQueries.get_all_podcasts().executeAsList()
    }

    fun getAllPodcastsWithEpisodes(): Map<Podcast, List<Episode>> {
        val podcasts = db.podcastsQueries.get_all_podcasts().executeAsList()
        return podcasts.associateWith {
            db.episodesQueries.get_episodes_by_channel_id(it.rss_link).executeAsList()
        }
    }

    fun listenForPodcasts(listener: Query.Listener) {
        db.podcastsQueries.get_all_podcasts().addListener(listener)
    }

    fun listenForEpisodes(channelId: String, listener: Query.Listener) {
        db.episodesQueries.get_episodes_by_channel_id(channelId).addListener(listener)
    }

    private fun String.sanitizeUrl(): String = replace("&amp;", "&")
    private fun String.sanitizeHtml(): String = Html.fromHtml(this).toString()
    private fun String?.orNow(): String = TODO()

    private fun String.formatDate(): String {
        val localDateTime = LocalDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME)
        val displayFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        return localDateTime.format(displayFormat)
    }
}