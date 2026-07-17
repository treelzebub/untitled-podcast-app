package net.treelzebub.podcasts.net.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.treelzebub.podcasts.data.PodcastPref
import net.treelzebub.podcasts.data.Prefs


@HiltWorker
class SyncPodcastsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val prefs: Prefs,
    private val subscriptionUpdater: SubscriptionUpdater
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        subscriptionUpdater.updateAll()
        prefs.putLong(PodcastPref.LastSyncTimestamp, System.currentTimeMillis())
        return Result.success()
    }
}
