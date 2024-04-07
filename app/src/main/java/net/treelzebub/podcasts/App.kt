package net.treelzebub.podcasts

import android.app.Application
import android.os.StrictMode
import android.os.strictmode.Violation
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import net.treelzebub.podcasts.net.sync.Sync


@HiltAndroidApp
class App : Application() {

    init {
        if (BuildConfig.DEBUG) {
//            TrafficStats.getAndSetThreadStatsTag(10000)
//            StrictMode.setVmPolicy(
//                VmPolicy.Builder()
//                    .detectAll()
//                    .penaltyLog()
//                    .penaltyListener(Executors.newSingleThreadExecutor()) {
//                        // https://github.com/square/okhttp/issues/3537
//                        it.ignore("onUntaggedSocket")
//                        it.ignore("UntaggedSocketViolation")
//                    }
//                    .build())
        }
    }

    override fun onCreate() {
        super.onCreate()
        Sync.initialize(this)
    }
}

private fun Violation.ignore(ignored: String) {
    val violation = stackTrace[0].toString()
    if (ignored !in violation) {
        Log.d(StrictMode::class.simpleName, violation, this)
    }
}