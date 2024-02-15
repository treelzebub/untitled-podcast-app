package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.data.PodcastRssHandler
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.RssHandler


@InstallIn(SingletonComponent::class)
@Module
class RepoModule {

    @Provides
    fun rssHandler(): RssHandler = PodcastRssHandler()

    @Provides
    fun podcastsRepo(rssHandler: RssHandler, db: Database): PodcastsRepo {
        return PodcastsRepo(rssHandler, db)
    }
}