package net.treelzebub.podcasts.util

import net.treelzebub.podcasts.BuildConfig
import android.util.Log as AndroidLog


// Quick and dirty logging for non-prod builds.
// If need arises, will migrate to Timber
object Log {

    fun d(tag: String, msg: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) AndroidLog.d(tag, msg, throwable)
    }

    fun e(tag: String, msg: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) AndroidLog.e(tag, msg, throwable)
    }
}
