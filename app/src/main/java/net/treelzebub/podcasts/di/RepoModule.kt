package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.BuildConfig
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.net.PodcastRssHandler
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.RssHandler
import net.treelzebub.podcasts.util.okHttpClient
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.util.concurrent.TimeUnit


@InstallIn(SingletonComponent::class)
@Module
class RepoModule {
    companion object {
        private const val TIMEOUT = 90L
        private const val MAX_IDLE_CONNECTIONS = 5
    }
    @Provides
    fun okHttpClient(): OkHttpClient {
        val logLevel = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
        return okHttpClient {
            connectionPool(ConnectionPool(MAX_IDLE_CONNECTIONS, TIMEOUT, TimeUnit.SECONDS))
            addInterceptor(HttpLoggingInterceptor().also { it.setLevel(logLevel) })
        }
    }

    @Provides
    fun rssHandler(): RssHandler = PodcastRssHandler(okHttpClient())

    @Provides
    fun podcastsRepo(rssHandler: RssHandler, db: Database): PodcastsRepo {
        return PodcastsRepo(rssHandler, db)
    }
}