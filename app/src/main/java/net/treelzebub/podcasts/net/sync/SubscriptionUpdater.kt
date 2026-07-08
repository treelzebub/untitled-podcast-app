package net.treelzebub.podcasts.net.sync

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.util.await
import net.treelzebub.podcasts.util.request
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


data class SyncResult(val total: Int, val succeeded: Int)

@Singleton
class SubscriptionUpdater @Inject constructor(
    private val client: OkHttpClient,
    private val repo: PodcastsRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun updateAll(): SyncResult = withContext(ioDispatcher) {
        val subs = repo.getAllRssLinks()
        val results = coroutineScope {
            subs.map { sub -> async { fetchAndUpsert(sub) } }.awaitAll()
        }
        val succeeded = results.count { it }
        Timber.d("Updated $succeeded of ${subs.size} podcasts.")
        SyncResult(subs.size, succeeded)
    }

    private suspend fun fetchAndUpsert(sub: SubscriptionDto): Boolean {
        return try {
            val response = client.newCall(request { get(); url(sub.rssLink) }).await()
            response.use { r ->
                val body = r.body?.string()
                if (body == null) {
                    Timber.e("Empty response body for feed: ${sub.rssLink}")
                    false
                } else {
                    val pair = repo.parseRssFeed(sub.rssLink, body)
                    repo.upsertPodcast(pair)
                    true
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating feed: ${sub.rssLink}")
            false
        }
    }
}
