package net.treelzebub.podcasts.net

import net.treelzebub.podcasts.BuildConfig
import net.treelzebub.podcasts.util.okHttpClient
import okhttp3.ConnectionPool
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.util.concurrent.TimeUnit


private const val TIMEOUT = 30L
private const val MAX_IDLE_CONNECTIONS = 5
private const val HEADER_KEY_USER_AGENT = "user-agent"
private const val HEADER_VALUE_USER_AGENT = BuildConfig.USER_AGENT
private const val HEADER_KEY_CONTENT_TYPE = "content-type"
private const val HEADER_VALUE_CONTENT_TYPE = "application/rss+xml"

private fun OkHttpClient.Builder.maybeLog() =
    if (BuildConfig.OK_LOGS) addInterceptor(HttpLoggingInterceptor().apply { setLevel(Level.HEADERS) }) else this

private fun Headers.Builder.userAgent() = add(HEADER_KEY_USER_AGENT, HEADER_VALUE_USER_AGENT)

val PodcastIndexOkClient = okHttpClient {
    addInterceptor(PodcastIndexHeadersInterceptor())
    .maybeLog()
}

val RssFetchingOkClient = okHttpClient {
    connectionPool(ConnectionPool(MAX_IDLE_CONNECTIONS, TIMEOUT, TimeUnit.SECONDS))
    addInterceptor { chain ->
        val headers = Headers.Builder()
            .add(HEADER_KEY_CONTENT_TYPE, HEADER_VALUE_CONTENT_TYPE)
            .userAgent()
            .build()
        val request = chain.request().newBuilder()
            .headers(headers)
            .build()
        chain.proceed(request)
    }
    .maybeLog()
}
