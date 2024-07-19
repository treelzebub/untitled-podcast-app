package net.treelzebub.podcasts.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.data.Prefs


@Module
@InstallIn(SingletonComponent::class)
class PrefsModule {

    @Provides
    fun prefs(app: Application) = Prefs(app)
}
