package net.treelzebub.podcasts.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.treelzebub.podcasts.data.Prefs


@Module
@InstallIn(SingletonComponent::class)
class PrefsModule {

    @Provides
    fun prefs(app: Application, @IoDispatcher ioDispatcher: CoroutineDispatcher) = Prefs(app, ioDispatcher)
}
