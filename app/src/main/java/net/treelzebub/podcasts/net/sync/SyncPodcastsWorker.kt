package net.treelzebub.podcasts.net.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.dataStore
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
    // TODO private val prefs: UserPreferences,
    private val podcastsRepo: PodcastsRepo,
    private val updater: SubscriptionUpdater,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        fun request() = PeriodicWorkRequestBuilder<SyncPodcastsWorker>(Duration.ofHours(1L)).build()
    }

    private val dataStore = appContext.dataStore
    private val prefLastSync = longPreferencesKey("last-sync-timestamp")
    private var shouldSync = false

    init {
        CoroutineScope(coroutineContext).launch {
            dataStore.data.collect { prefs ->
                val _15_minutes_ago = Time.nowSeconds() - (15 * 60)
                shouldSync = (prefs[prefLastSync] ?: _15_minutes_ago) <= _15_minutes_ago
            }
        }
    }

    // TODO: Improve by defining freshness. Pull all pods, only update when stale.
    override suspend fun doWork(): Result {
        if (!shouldSync) return Result.success()

        Timber.d("Starting sync...")
        val subs = podcastsRepo.getAllRssLinks()
        Timber.d("Processing updates for ${subs.size} podcasts...")
        subs.forEach { sub ->
            Timber.d("Fetching Feed for ${sub.rssLink}")
            val onFailure: (Call, IOException) -> Unit = { _, e ->
                // TODO error propagation
                Timber.e("Error Updating Feed with url: ${sub.rssLink}", e)
            }
            val onResponse: (Call, Response) -> Unit = { call, response ->
                if (response.isSuccessful && response.body != null) {
                    Timber.d("Updated Feed with url: ${sub.rssLink}. Parsing...")
                    CoroutineScope(ioDispatcher).launch {
                        val parsed = podcastsRepo.parseRssFeed(response.body!!.string())
                        podcastsRepo.upsertPodcast(sub.rssLink, parsed)
                    }
                    Timber.d("Parsed and persisted Feed with url: ${sub.rssLink}")
                } else onFailure(call, IOException("Unknown Error"))
            }
            updater.update(sub, onResponse, onFailure)
        }
        dataStore.edit { prefs ->
            prefs[prefLastSync] = Time.nowSeconds()
            Timber.d("Pref time updated: ${Time.verboseFormat(Time.nowSeconds())}")
        }
        // TODO
        return Result.success()
    }
}
