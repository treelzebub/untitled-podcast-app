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


@Singleton
class SubscriptionUpdater @Inject constructor(
    private val client: OkHttpClient,
    private val repo: PodcastsRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val fetchedPodcasts = ConcurrentHashMap<String, Pair<Podcast, List<Episode>>>()
    private lateinit var latch: CountDownLatch

    fun updateAll(onFailure: (SubscriptionDto, Call, IOException) -> Unit, onComplete: () -> Unit = {}) {
        scope.launch {
            val subs = repo.getAllRssLinks()
            latch = CountDownLatch(subs.size)
            subs.forEach { update(it, onFailure) }
            latch.await()

            val old = repo.getPodcasts().associateBy { it.id }.toMap()
            val new = fetchedPodcasts.toMap()
            val podcastsForUpdate = idsForUpdate(old, new).mapNotNull { new[it] }
            podcastsForUpdate.forEach { repo.upsertPodcast(it) }

            Timber.d("Updated ${podcastsForUpdate.size} of ${subs.size} podcasts.")
            fetchedPodcasts.clear()
            onComplete()
        }
    }

    private fun idsForUpdate(old: Map<String, Podcast>, new: Map<String, Pair<Podcast, List<Episode>>>): List<String> {
        val podcastIdToLatestTimestamp = new.map {
            // Null maxOf means we have an empty episodes list, and should update db accordingly.
            it.key to (it.value.second.maxOfOrNull { episode -> episode.date } ?: Long.MAX_VALUE)
        }.toMap()
        return podcastIdToLatestTimestamp.mapNotNull {
            if (old[it.key]!!.latest_episode_timestamp < it.value) {
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
                        fetchedPodcasts += pair.first.id to pair
                    }
                    response.close()
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
