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
    @Assisted app: Context,
    @Assisted params: WorkerParameters,
    // TODO private val prefs: UserPreferences,
    private val podcastsRepo: PodcastsRepo,
    private val updater: SubscriptionUpdater,
) : CoroutineWorker(app, params) {

    companion object {
        private val TAG = SyncPodcastsWorker::class.java.simpleName

        fun worker() = PeriodicWorkRequestBuilder<SyncPodcastsWorker>(Duration.ofHours(12L)).build()
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync...")
        val subs = podcastsRepo.getAllAsList()
            .map { SubscriptionDto(it.id, it.rssLink) }
        subs.forEach { sub ->
            val onFailure: (Call, IOException) -> Unit = { _, e ->
                Log.e(TAG, "Error Updating Feed. Url: ${sub.rssLink}. Error:", e)
            }
            val onResponse: (Call, Response) -> Unit = { call, response ->
                if (response.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val parsed = podcastsRepo.parseRssFeed(response.body!!.string())
                        podcastsRepo.upsert(sub.rssLink, parsed)
                    }
                } else onFailure(call, IOException("Unknown Error"))
            }
            updater.update(sub, onResponse, onFailure)
        }

        // TODO 
        return Result.success()
    }
}
