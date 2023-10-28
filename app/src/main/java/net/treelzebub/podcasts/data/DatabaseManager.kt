package net.treelzebub.podcasts.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.prof18.rssparser.model.ItunesChannelData
import com.prof18.rssparser.model.ItunesItemData
import com.prof18.rssparser.model.RssChannel
import net.treelzebub.podcasts.Channel
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.Episode

class DatabaseManager(context: Context) {

    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context.applicationContext, "podcasts.db")
    private val itunesItemDataAdapter = SerializedColumnAdapter(MoshiSerializer(ItunesItemData::class.java))
    private val itunesChannelDataAdapter = SerializedColumnAdapter(MoshiSerializer(ItunesChannelData::class.java))
    private val db = Database(
        driver,
        Channel.Adapter(itunesChannelDataAdapter),
        Episode.Adapter(itunesItemDataAdapter)
    )

    // TODO remove after debugging
    val episodeQueries = db.episodesQueries
    val channelQueries = db.channelsQueries

    // TODO null safety
    suspend fun insert(rssLink: String, channel: RssChannel) {
        db.transaction {
            with(channel) {
                db.channelsQueries.insert(
                    link!!.sanitize(), rssLink.sanitize(), title!!, link!!.sanitize(), description!!, image?.url?.sanitize(),
                    lastBuildDate, updatePeriod, itunesChannelData
                )
            }
            channel.items.forEach {
                with(it) {
                    db.episodesQueries.insert(
                        guid!!, channel.link!!.sanitize(), channel.title!!, title!!, author.orEmpty(), link!!.sanitize(), pubDate, description,
                        content, channel.image?.url?.sanitize(), audio, sourceName, sourceUrl?.sanitize(), itunesItemData, commentsUrl?.sanitize()
                    )
                }
            }
        }
    }

    suspend fun getEpisodesFromChannel(channelId: String): List<Episode> {
        return db.episodesQueries.get_episodes_by_channel_id(channelId).executeAsList()
    }

    suspend fun getAllChannels(): List<Channel> {
        return db.channelsQueries.get_all_channels().executeAsList()
    }

    fun getAllChannelsWithEpisodes(): Map<Channel, List<Episode>> {
        val channels = db.channelsQueries.get_all_channels().executeAsList()
        return channels.associateWith {
            db.episodesQueries.get_episodes_by_channel_id(it.rss_link).executeAsList()
        }
    }

    private fun String.sanitize(): String = replace("&amp;", "&")
}