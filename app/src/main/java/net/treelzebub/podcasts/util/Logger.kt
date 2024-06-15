package net.treelzebub.podcasts.util

import net.treelzebub.podcasts.BuildConfig
import timber.log.Timber
import android.util.Log as AndroidLog


// Quick and dirty logging for non-prod builds.
// If need arises, will migrate to Timber
object Logger {

    fun d(msg: String, throwable: Throwable? = null) {
        Timber.d(msg, throwable)
    }

    fun e(msg: String, throwable: Throwable) {
        Timber.e(msg, throwable)
    }
}
