package net.treelzebub.podcasts.net.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.treelzebub.podcasts.data.PodcastPref
import net.treelzebub.podcasts.data.Prefs
import net.treelzebub.podcasts.net.models.SubscriptionDto
import okhttp3.Call
import timber.log.Timber
import java.io.IOException


@HiltWorker
class SyncPodcastsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val prefs: Prefs,
    private val subscriptionUpdater: SubscriptionUpdater,
    private val timestampUpdater: TimestampUpdater
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val onFailure: (SubscriptionDto, Call, IOException) -> Unit = { sub, _, e ->
            Timber.e("Error Updating Feed with url: ${sub.rssLink}", e) // TODO
        }
        subscriptionUpdater.updateAll(onFailure = onFailure)
        timestampUpdater.update()
        prefs.putLong(PodcastPref.LastSyncTimestamp, System.currentTimeMillis())
        return Result.success()
    }
}
