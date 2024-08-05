package net.treelzebub.podcasts.net

import net.treelzebub.podcasts.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import java.time.Instant.now
import java.util.Locale


class PodcastIndexHeadersInterceptor : Interceptor {

    companion object {
        private const val USER_AGENT = BuildConfig.USER_AGENT
        private const val API_KEY = BuildConfig.API_KEY_PODCAST_INDEX
        private const val API_SECRET = BuildConfig.API_SECRET_PODCAST_INDEX

        private val now: String
            get() = now().epochSecond.toString()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val timestamp = now
        val request = chain.request()
            .newBuilder()
            .addHeader("User-Agent", USER_AGENT)
            .addHeader("X-Auth-Key", API_KEY)
            .addHeader("X-Auth-Date", timestamp)
            .addHeader("Authorization", authorization(timestamp))
            .build()
        return chain.proceed(request)
    }

    private fun authorization(now: String): String {
        val sha1 = MessageDigest.getInstance("SHA1")
        val input = "$API_KEY$API_SECRET$now"
        sha1.update(input.toByteArray())

        val digest = sha1.digest()
        val buffer = StringBuilder()

        for (byte in digest) {
            buffer.append(String.format(Locale.getDefault(), "%02x", byte))
        }
        return buffer.toString()
    }
}
