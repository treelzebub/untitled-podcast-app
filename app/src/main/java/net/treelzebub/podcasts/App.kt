package net.treelzebub.podcasts

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.prof18.rssparser.model.ItunesChannelData
import com.prof18.rssparser.model.ItunesItemData
import net.treelzebub.podcasts.data.MoshiSerializer
import net.treelzebub.podcasts.data.SerializedColumnAdapter

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // db.channelsQueries.get_episodes(channelId).executeAsList()
    }
}