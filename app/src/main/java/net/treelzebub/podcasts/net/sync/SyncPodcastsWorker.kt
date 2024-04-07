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
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.util.Log
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

        fun request() = PeriodicWorkRequestBuilder<SyncPodcastsWorker>(Duration.ofHours(12L)).build()
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync...")
        val subs = podcastsRepo.getAllAsList()
            .map { SubscriptionDto(it.id, it.rssLink) }
        Log.d(TAG, "Processing updates for ${subs.size} podcasts...")
        subs.forEach { sub ->
            Log.d(TAG, "Fetching Feed for ${sub.rssLink}")
            val onFailure: (Call, IOException) -> Unit = { _, e ->
                // TODO error propagation
                Log.e(TAG, "Error Updating Feed with url: ${sub.rssLink}. Error:", e)
            }
            val onResponse: (Call, Response) -> Unit = { call, response ->
                if (response.isSuccessful && response.body != null) {
                    Log.d(TAG, "Updated Feed with url: ${sub.rssLink}. Parsing...")
                    CoroutineScope(Dispatchers.IO).launch {
                        val parsed = podcastsRepo.parseRssFeed(response.body!!.string())
                        podcastsRepo.upsert(sub.rssLink, parsed)
                    }
                    Log.d(TAG, "Parsed and persisted Feed with url: ${sub.rssLink}")
                } else onFailure(call, IOException("Unknown Error"))
            }
            updater.update(sub, onResponse, onFailure)
        }

        // TODO
        return Result.success()
    }
}
