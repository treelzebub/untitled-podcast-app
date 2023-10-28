package net.treelzebub.podcasts

import android.app.Application
import net.treelzebub.podcasts.data.DatabaseManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        DatabaseManager.init(this)
    }
}