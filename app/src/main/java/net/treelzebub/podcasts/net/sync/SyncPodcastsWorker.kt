package net.treelzebub.podcasts.net.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.treelzebub.podcasts.data.PodcastPref
import net.treelzebub.podcasts.data.Prefs
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.util.Time
import okhttp3.Call
import timber.log.Timber
import java.io.IOException
import java.time.Duration


@HiltWorker
class SyncPodcastsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val prefs: Prefs,
    private val updater: SubscriptionUpdater,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        fun request() = PeriodicWorkRequestBuilder<SyncPodcastsWorker>(Duration.ofHours(1L)).build()
    }

    private var shouldSync = true

    init {
        prefs.collect(PodcastPref.LastSyncTimestamp) {
            val _15_minutes_ago = Time.nowSeconds() - (15 * 60)
            shouldSync = it <= _15_minutes_ago
        }
    }

    override suspend fun doWork(): Result {
        if (!shouldSync) return Result.success()
        val onFailure: (SubscriptionDto, Call, IOException) -> Unit = { sub, _, e ->
            Timber.e("Error Updating Feed with url: ${sub.rssLink}", e) // TODO
        }
        updater.updateAll(onFailure)
        prefs.edit(PodcastPref.LastSyncTimestamp, Time.nowSeconds())
        return Result.success()
    }
}
