package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.media.manager.MediaManager
import net.treelzebub.podcasts.media.manager.MediaManagerInterface
import net.treelzebub.podcasts.media.player.MediaControllerWrapper
import net.treelzebub.podcasts.media.player.PlayerControllerInterface


@Module
@InstallIn(SingletonComponent::class)
class MediaModule {

    @Provides
    fun providePlayerControllerInterface(
        @MainDispatcher mainDispatcher: CoroutineDispatcher,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PlayerControllerInterface {
        return MediaControllerWrapper(mainDispatcher, ioDispatcher)
    }

    @Provides
    fun provideMediaManagerInterface(
        @MainDispatcher mainDispatcher: CoroutineDispatcher,
        playerController: PlayerControllerInterface
    ): MediaManagerInterface {
        return MediaManager(mainDispatcher, playerController)
    }
}