package net.treelzebub.podcasts.net.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastPref
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.Prefs
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.util.Time
import okhttp3.Call
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.time.Duration


@HiltWorker
class SyncPodcastsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val prefs: Prefs,
    private val podcastsRepo: PodcastsRepo,
    private val updater: SubscriptionUpdater,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        fun request() = PeriodicWorkRequestBuilder<SyncPodcastsWorker>(Duration.ofHours(1L)).build()
    }

    private var shouldSync = false

    init {
        prefs.collect(PodcastPref.LastSyncTimestamp) {
            val _15_minutes_ago = Time.nowSeconds() - (15 * 60)
            shouldSync = it <= _15_minutes_ago
        }
    }

    // TODO: Improve by defining freshness. Pull all pods, only update when stale.
    override suspend fun doWork(): Result {
        if (!shouldSync) return Result.success()

        val subs = podcastsRepo.getAllRssLinks()
        subs.forEach { sub ->
            val onFailure: (Call, IOException) -> Unit = { _, e ->
                Timber.e("Error Updating Feed with url: ${sub.rssLink}", e) // TODO
            }
            val onResponse: (Call, Response) -> Unit = { call, response ->
                if (response.isSuccessful && response.body != null) {
                    CoroutineScope(ioDispatcher).launch {
                        val parsed = podcastsRepo.parseRssFeed(response.body!!.string())
                        podcastsRepo.upsertPodcast(sub.rssLink, parsed)
                    }
                } else onFailure(call, IOException("Unknown Error"))
            }
            updater.update(sub, onResponse, onFailure)
        }
        prefs.edit(PodcastPref.LastSyncTimestamp, Time.nowSeconds())
        return Result.success()
    }
}
