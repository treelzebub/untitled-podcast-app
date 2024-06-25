package net.treelzebub.podcasts.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.treelzebub.podcasts.data.MoshiSerializer
import net.treelzebub.podcasts.data.PodcastQueue
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.data.StringSerializer


@Module
@InstallIn(SingletonComponent::class)
class StoreModule {

    @Provides
    fun moshiSerializer(): StringSerializer<PodcastQueue> {
        return MoshiSerializer(PodcastQueue::class.java)
    }

    @Provides
    fun queueStore(
        app: Application,
        moshiSerializer: StringSerializer<PodcastQueue>,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): QueueStore {
        return QueueStore(app, moshiSerializer, ioDispatcher)
    }
}
