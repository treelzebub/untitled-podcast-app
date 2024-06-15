package net.treelzebub.podcasts.util

import timber.log.Timber


object Logger {

    fun d(msg: String, throwable: Throwable? = null) {
        Timber.d(msg, throwable)
    }

    fun e(msg: String, throwable: Throwable) {
        Timber.e(msg, throwable)
    }
}
