package net.treelzebub.podcasts.net.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.util.Logger
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import java.time.Duration


@HiltWorker
class SyncPodcastsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    // TODO private val prefs: UserPreferences,
    private val podcastsRepo: PodcastsRepo,
    private val updater: SubscriptionUpdater,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private val TAG = SyncPodcastsWorker::class.java.simpleName

        fun request() = PeriodicWorkRequestBuilder<SyncPodcastsWorker>(Duration.ofHours(1L)).build()
    }

    // TODO: Improve by defining freshness. Pull all pods, only update when stale.
    override suspend fun doWork(): Result {
        Logger.d(TAG, "Starting sync...")
        val subs = podcastsRepo.getAllRssLinks()
        Logger.d(TAG, "Processing updates for ${subs.size} podcasts...")
        subs.forEach { sub ->
            Logger.d(TAG, "Fetching Feed for ${sub.rssLink}")
            val onFailure: (Call, IOException) -> Unit = { _, e ->
                // TODO error propagation
                Logger.e(TAG, "Error Updating Feed with url: ${sub.rssLink}. Error:", e)
            }
            val onResponse: (Call, Response) -> Unit = { call, response ->
                if (response.isSuccessful && response.body != null) {
                    Logger.d(TAG, "Updated Feed with url: ${sub.rssLink}. Parsing...")
                    CoroutineScope(Dispatchers.IO).launch {
                        val parsed = podcastsRepo.parseRssFeed(response.body!!.string())
                        podcastsRepo.insertOrReplacePodcast(sub.rssLink, parsed)
                    }
                    Logger.d(TAG, "Parsed and persisted Feed with url: ${sub.rssLink}")
                } else onFailure(call, IOException("Unknown Error"))
            }
            updater.update(sub, onResponse, onFailure)
        }

        // TODO
        return Result.success()
    }
}
