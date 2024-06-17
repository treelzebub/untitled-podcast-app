package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore


@Module
@InstallIn(SingletonComponent::class)
class StoreModule {

    @Provides
    fun queueStore(repo: PodcastsRepo): QueueStore = QueueStore(repo)
}
