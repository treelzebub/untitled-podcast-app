package net.treelzebub.podcasts.net.sync

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.util.request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton


// TODO everything is suspend fun in repo too
//      and add logging to sync to report how many were updated and ignored,.
@Singleton
class SubscriptionUpdater @Inject constructor(
    private val client: OkHttpClient,
    private val repo: PodcastsRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val fetchedPodcasts = ConcurrentHashMap<String, List<Episode>>()
    private lateinit var latch: CountDownLatch

    fun updateAll(onComplete: () -> Unit = {}, onFailure: (SubscriptionDto, Call, IOException) -> Unit) {
        scope.launch {
            val subs = repo.getAllRssLinks()
            latch = CountDownLatch(subs.size)
            subs.forEach { update(it, onFailure) }
            latch.await()
            /**
             * 1. Fetch all feeds, parse.
             * 2. Compare Podcast objects: if timestamp of existing is less than timestamp of new, update pod and its episodes.
             */

            val old = repo.getPodcasts().associateBy { it.id }.toMap()
            val new = fetchedPodcasts.toMap()
            val updateIds = updateIds(old, new)
            val diff = updateIds.map {
                val podcast = new[it]!!
                podcast to fetchedPodcasts[podcast]
            }

            diff.forEach { repo.upsertPodcast(it) }

            Timber.d("Updated ${diff.size} of ${subs.size} podcasts.")
            fetchedPodcasts.clear()
            onComplete()
        }
    }

    private fun updateIds(old: Map<String, Podcast>, new: Map<String, Podcast>): List<String> {
        return new.mapNotNull {
            val existing = old[it.key]
            if (existing == null || existing.latest_episode_timestamp < it.value.latest_episode_timestamp) {
                it.key
            } else null
        }
    }

    private suspend fun update(
        sub: SubscriptionDto,
        onFailure: (SubscriptionDto, Call, IOException) -> Unit
    ) {
        val request = request {
            get()
            url(sub.rssLink)
        }
        val callback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                scope.launch {
                    response.body?.let {
                        val pair = repo.parseRssFeed(sub.rssLink, it.string())
                        fetchedPodcasts += pair
                    }
                    latch.countDown()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                latch.countDown()
                Timber.e(e)
                onFailure(sub, call, e)
            }
        }
        client.newCall(request).enqueue(callback)
    }

    fun cancelAll() = client.dispatcher.cancelAll()
}
