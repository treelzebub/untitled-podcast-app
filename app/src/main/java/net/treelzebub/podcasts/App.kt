package net.treelzebub.podcasts

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {

    init {
        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}