package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.net.PodcastIndexService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Provides
    fun podcastIndexService(): PodcastIndexService {
        val interceptor = HttpLoggingInterceptor().also {
            it.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder()
            .baseUrl("https://podcastindex.org")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PodcastIndexService::class.java)
    }
}