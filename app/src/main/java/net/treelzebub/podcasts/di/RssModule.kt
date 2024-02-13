package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.data.RssHandler
import net.treelzebub.podcasts.data.PodcastRssHandler


@InstallIn(SingletonComponent::class)
@Module
class RssModule {

    @Provides
    fun rssHandler(): RssHandler = PodcastRssHandler()
}