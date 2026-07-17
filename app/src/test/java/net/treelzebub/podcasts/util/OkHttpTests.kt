package net.treelzebub.podcasts.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.ConnectException

class OkHttpTests {

    private lateinit var server: MockWebServer
    private val client = OkHttpClient()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `await resumes with the response on success`() = runTest {
        server.enqueue(MockResponse().setBody("hello").setResponseCode(200))

        val call = client.newCall(request { get(); url(server.url("/feed")) })
        val response = call.await()

        assertEquals(200, response.code)
        assertEquals("hello", response.body?.string())
    }

    @Test
    fun `await resumes with an exception on network failure`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val call = client.newCall(request { get(); url(server.url("/feed")) })
        try {
            call.await()
            org.junit.Assert.fail("Expected an IOException")
        } catch (e: IOException) {
            // Expected: OkHttp surfaces the dropped connection as an IOException.
        }
    }

    @Test
    fun `await propagates an exception when the host is unreachable`() = runTest {
        server.shutdown()

        val call = client.newCall(request { get(); url(server.url("/feed")) })
        try {
            call.await()
            org.junit.Assert.fail("Expected a ConnectException")
        } catch (e: ConnectException) {
            // Expected: nothing is listening on that port anymore.
        }
    }

    @Test
    fun `cancelling the coroutine cancels the underlying call`() = runTest {
        server.enqueue(
            MockResponse().setBody("slow").setBodyDelay(10, java.util.concurrent.TimeUnit.SECONDS)
        )
        val call = client.newCall(request { get(); url(server.url("/feed")) })

        val scope = CoroutineScope(Dispatchers.Default)
        val job = scope.launch { call.await() }
        // Give the call a moment to actually be enqueued before cancelling it.
        withTimeout(5000) {
            while (!call.isExecuted()) kotlinx.coroutines.delay(1)
        }
        job.cancel()
        job.join()

        assertTrue(call.isCanceled())
        scope.cancel()
    }
}
