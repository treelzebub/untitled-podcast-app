package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.data.RssHandler


@Module
@InstallIn(SingletonComponent::class)
class RepoModule {

    @Provides
    fun podcastsRepo(
        rssHandler: RssHandler,
        db: Database,
        queueStore: QueueStore,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PodcastsRepo {
        return PodcastsRepo(rssHandler, db, queueStore, ioDispatcher)
    }
}
