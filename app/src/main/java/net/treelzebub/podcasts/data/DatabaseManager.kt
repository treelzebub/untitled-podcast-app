package net.treelzebub.podcasts.data

import android.content.Context
import android.text.Html
import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.prof18.rssparser.model.ItunesChannelData
import com.prof18.rssparser.model.ItunesItemData
import com.prof18.rssparser.model.RssChannel
import dagger.hilt.android.qualifiers.ApplicationContext
import net.treelzebub.podcasts.Channel
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.Episode
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
        val itunesItemDataAdapter = SerializedColumnAdapter(MoshiSerializer(ItunesItemData::class.java))
        val itunesChannelDataAdapter = SerializedColumnAdapter(MoshiSerializer(ItunesChannelData::class.java))
        db = Database(driver,
            Channel.Adapter(itunesChannelDataAdapter),
            Episode.Adapter(itunesItemDataAdapter)
        )
    }

    // TODO null safety
    fun insert(rssLink: String, channel: RssChannel) {
        db.transaction {
            with(channel) {
                db.channelsQueries.insert(
                    link!!.sanitizeUrl(), rssLink.sanitizeUrl(), title!!, link!!.sanitizeUrl(), description!!.sanitizeHtml(),
                    image?.url?.sanitizeUrl(), lastBuildDate, updatePeriod, itunesChannelData
                )
            }
            channel.items.forEach {
                with(it) {
                    db.episodesQueries.insert(
                        guid!!, channel.link!!.sanitizeUrl(), channel.title!!, title!!, author.orEmpty(), link!!.sanitizeUrl(),
                        pubDate!!.formatDate(), description!!.sanitizeHtml(), content, channel.image?.url?.sanitizeUrl(),
                        audio, sourceName, sourceUrl?.sanitizeUrl(), itunesItemData, commentsUrl?.sanitizeUrl()
                    )
                }
            }
        }
    }

    fun getEpisodesFromChannel(channelId: String): List<Episode> {
        return db.episodesQueries.get_episodes_by_channel_id(channelId).executeAsList()
    }

    fun getAllChannels(): List<Channel> {
        return db.channelsQueries.get_all_channels().executeAsList()
    }

    fun getAllChannelsWithEpisodes(): Map<Channel, List<Episode>> {
        val channels = db.channelsQueries.get_all_channels().executeAsList()
        return channels.associateWith {
            db.episodesQueries.get_episodes_by_channel_id(it.rss_link).executeAsList()
        }
    }

    fun listenForEpisodes(listener: Query.Listener) {
        db.episodesQueries.get_all_episodes().addListener(listener)
    }

    private fun String.sanitizeUrl(): String = replace("&amp;", "&")
    private fun String.sanitizeHtml(): String = Html.fromHtml(this).toString()

    private fun String.formatDate(): String {
        val localDateTime = LocalDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME)
        val displayFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        return localDateTime.format(displayFormat)
    }
}