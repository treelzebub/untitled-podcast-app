package net.treelzebub.podcasts.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.data.RssHandler
import net.treelzebub.podcasts.net.PodcastIndexHeadersInterceptor
import net.treelzebub.podcasts.net.PodcastIndexService
import net.treelzebub.podcasts.net.PodcastRssHandler
import net.treelzebub.podcasts.net.models.PodcastsOkClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun okHttpClient(): OkHttpClient = PodcastsOkClient

    @Provides
    fun rssHandler(): RssHandler = PodcastRssHandler(okHttpClient())

    @Provides
    fun podcastIndexService(): PodcastIndexService {
        val Timber = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        val headers = PodcastIndexHeadersInterceptor()
        val client = OkHttpClient.Builder().addInterceptor(Timber).addInterceptor(headers).build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        return Retrofit.Builder()
            .baseUrl("https://api.podcastindex.org/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(PodcastIndexService::class.java)
    }
}
