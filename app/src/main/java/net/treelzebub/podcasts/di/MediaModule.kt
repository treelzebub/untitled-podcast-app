package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.treelzebub.podcasts.PodcastsQueries
import net.treelzebub.podcasts.media.PlayerManager


@Module
@InstallIn(SingletonComponent::class)
class MediaModule {

    @Provides
    fun providePlayerManager(
        @MainDispatcher mainDispatcher: CoroutineDispatcher,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        podcastQueries: PodcastsQueries
    ): PlayerManager {
        return PlayerManager(mainDispatcher, ioDispatcher, podcastQueries)
    }
}