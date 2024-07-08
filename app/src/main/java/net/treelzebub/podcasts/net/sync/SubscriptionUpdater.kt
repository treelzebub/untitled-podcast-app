package net.treelzebub.podcasts.net.sync

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.net.models.SubscriptionDto
import net.treelzebub.podcasts.util.request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SubscriptionUpdater @Inject constructor(
    private val client: OkHttpClient,
    private val repo: PodcastsRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun updateAll(onFailure: (SubscriptionDto, Call, IOException) -> Unit) {
        scope.launch {
            repo.getAllRssLinks().forEach { update(it, onFailure) }
        }
    }

    fun update(
        sub: SubscriptionDto,
        onFailure: (SubscriptionDto, Call, IOException) -> Unit
    ) {
        val request = request {
            get()
            url(sub.rssLink)
        }
        val callback = object : Callback {
            override fun onResponse(call: Call, response: Response) = onSuccess(sub, response)
            override fun onFailure(call: Call, e: IOException) = onFailure(sub, call, e)
        }

        client.newCall(request).enqueue(callback)
    }

    private fun onSuccess(sub: SubscriptionDto, response: Response) {
        scope.launch {
            val parsed = repo.parseRssFeed(response.body!!.string())
            repo.upsertPodcast(sub.rssLink, parsed)
        }
    }

    fun cancelAll() {
        client.dispatcher.cancelAll()
    }
}
