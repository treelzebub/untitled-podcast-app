package net.treelzebub.podcasts

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import coil.imageLoader
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import net.treelzebub.podcasts.net.models.RssFetchingOkClient
import net.treelzebub.podcasts.net.sync.Sync
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject


@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.WORK_LOGS) Log.DEBUG else Log.ASSERT)
            .build()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        Sync.initialize(this)
        initCoil()
    }

    private fun initCoil() {
        val builder = imageLoader.newBuilder()
            .crossfade(true)
            .crossfade(500)
            .okHttpClient(RssFetchingOkClient)
        if (BuildConfig.COIL_LOGS) builder.logger(DebugLogger())
        Coil.setImageLoader(builder.build())
    }
}
