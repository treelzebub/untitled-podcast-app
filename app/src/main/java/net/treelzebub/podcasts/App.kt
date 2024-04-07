package net.treelzebub.podcasts

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import net.treelzebub.podcasts.net.sync.Sync
import javax.inject.Inject


@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ASSERT)
            .build()

    override fun onCreate() {
        super.onCreate()
        Sync.initialize(this)
    }
}
