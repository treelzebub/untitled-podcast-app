package net.treelzebub.podcasts.di

import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.data.RssFeedHandler
import net.treelzebub.podcasts.data.RssParser


@InstallIn(SingletonComponent::class)
@Module
class RssModule {

    @Provides
    fun rssHandler(): RssFeedHandler = RssParser()
}