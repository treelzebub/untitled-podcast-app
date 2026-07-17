package net.treelzebub.podcasts.net.sync

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
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
        // Fetch concurrently, but hold results in memory: writing them all in one
        // transaction means the UI settles once, fully sorted, instead of once per feed.
        val fetched = coroutineScope {
            subs.map { sub -> async { fetchFeed(sub) } }.awaitAll()
        }.filterNotNull()
        repo.syncSubscriptions(fetched)
        Timber.d("Updated ${fetched.size} of ${subs.size} podcasts.")
        SyncResult(subs.size, fetched.size)
    }

    private suspend fun fetchFeed(sub: SubscriptionDto): Pair<Podcast, List<Episode>>? {
        return try {
            val response = client.newCall(request { get(); url(sub.rssLink) }).await()
            response.use { r ->
                val body = r.body?.string()
                if (body == null) {
                    Timber.e("Empty response body for feed: ${sub.rssLink}")
                    null
                } else {
                    repo.parseRssFeed(sub.rssLink, body)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating feed: ${sub.rssLink}")
            null
        }
    }
}
