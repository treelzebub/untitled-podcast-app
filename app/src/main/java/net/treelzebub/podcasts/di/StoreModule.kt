package net.treelzebub.podcasts.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.data.MoshiSerializer
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import net.treelzebub.podcasts.ui.models.EpisodeUi


@Module
@InstallIn(SingletonComponent::class)
class StoreModule {

    @Provides
    inline fun <reified T> moshiSerializer(): MoshiSerializer<T> {
        return MoshiSerializer(T::class.java)
    }

    @Provides
    fun queueStore(app: Application, moshiSerializer: MoshiSerializer<List<EpisodeUi>>): QueueStore {
        return QueueStore(app, moshiSerializer)
    }
}
