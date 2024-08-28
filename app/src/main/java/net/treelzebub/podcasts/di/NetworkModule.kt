package net.treelzebub.podcasts.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.data.RssHandler
import net.treelzebub.podcasts.net.PodcastIndexOkClient
import net.treelzebub.podcasts.net.PodcastIndexService
import net.treelzebub.podcasts.net.PodcastRssHandler
import net.treelzebub.podcasts.net.RssFetchingOkClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun okHttpClient(): OkHttpClient = RssFetchingOkClient

    @Provides
    fun rssHandler(): RssHandler = PodcastRssHandler(okHttpClient())

    @Provides
    fun podcastIndexService(): PodcastIndexService {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        return Retrofit.Builder()
            .baseUrl("https://api.podcastindex.org/")
            .client(PodcastIndexOkClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(PodcastIndexService::class.java)
    }
}
