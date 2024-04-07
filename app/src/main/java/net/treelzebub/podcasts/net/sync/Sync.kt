package net.treelzebub.podcasts.net.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager

object Sync {

    private const val WORK_NAME_PODCASTS = "sync-podcasts"

    fun initialize(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PODCASTS,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            SyncPodcastsWorker.request()
        )
    }
}