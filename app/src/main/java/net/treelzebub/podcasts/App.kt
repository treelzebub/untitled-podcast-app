package net.treelzebub.podcasts

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        // TODO rm
        lateinit var Instance: App
    }

    override fun onCreate() {
        super.onCreate()
        Instance = this
    }
}