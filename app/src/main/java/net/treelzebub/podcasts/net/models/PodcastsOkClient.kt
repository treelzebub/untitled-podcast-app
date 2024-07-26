package net.treelzebub.podcasts.net.models

import net.treelzebub.podcasts.BuildConfig
import net.treelzebub.podcasts.util.okHttpClient
import okhttp3.ConnectionPool
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.util.concurrent.TimeUnit


private const val TIMEOUT = 30L
private const val MAX_IDLE_CONNECTIONS = 5

val PodcastsOkClient = okHttpClient {
    connectionPool(ConnectionPool(MAX_IDLE_CONNECTIONS, TIMEOUT, TimeUnit.SECONDS))
    addInterceptor(Interceptor { chain ->
        val headers = Headers.Builder()
            .add("content-type", "application/rss+xml")
            .add("user-agent", "net.treelzebub.podcasts")
            .build()
        val request = chain.request().newBuilder()
            .headers(headers)
            .build()
        chain.proceed(request)
    })
    if (BuildConfig.DEBUG) {
       addInterceptor(HttpLoggingInterceptor().apply { setLevel(Level.HEADERS) })
    }
}
