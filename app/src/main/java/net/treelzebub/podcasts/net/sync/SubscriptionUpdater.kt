package net.treelzebub.podcasts.net.sync

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
) {

    fun update(
        sub: SubscriptionDto,
        onResponse: (Call, Response) -> Unit,
        onFailure: (Call, IOException) -> Unit
    ) {
        val request = request {
            get()
            url(sub.rssLink)
        }
        val callback = object : Callback {
            override fun onResponse(call: Call, response: Response) = onResponse(call, response)
            override fun onFailure(call: Call, e: IOException) = onFailure(call, e)
        }

        client.newCall(request).enqueue(callback)
    }

    fun cancelAll() = client.dispatcher.cancelAll()
}
