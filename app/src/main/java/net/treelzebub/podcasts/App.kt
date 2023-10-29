package net.treelzebub.podcasts

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.treelzebub.podcasts.data.DatabaseManager

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}