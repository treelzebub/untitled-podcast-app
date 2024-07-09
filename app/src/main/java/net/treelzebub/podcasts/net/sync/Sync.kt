package net.treelzebub.podcasts.net.sync

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import net.treelzebub.podcasts.data.PodcastPref
import net.treelzebub.podcasts.data.Prefs
import java.time.Duration

object Sync {

    private const val WORK_NAME_PODCASTS = "sync-podcasts"

    fun initialize(app: Application) {
        val prefs = Prefs(app)
        val builder = PeriodicWorkRequestBuilder<SyncPodcastsWorker>(Duration.ofHours(1L))
        val lastSync = prefs.getLong(PodcastPref.LastSyncTimestamp)
        // Constrain sync to once per hour, regardless of interim app launches.
        if (lastSync > -1L) builder.setNextScheduleTimeOverride(lastSync + (60 * 60 * 1000))

        WorkManager.getInstance(app).enqueueUniquePeriodicWork(
            WORK_NAME_PODCASTS,
            ExistingPeriodicWorkPolicy.UPDATE,
            builder.build()
        )
    }
}