package net.treelzebub.podcasts.net.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.util.TestCoroutines
import net.treelzebub.podcasts.util.TestRssHandler
import net.treelzebub.podcasts.util.podcastRepo
import net.treelzebub.podcasts.util.withDatabase
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionUpdaterTests {

    private lateinit var server: MockWebServer
    private val client = OkHttpClient()

    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutines.dispatcher)
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        server.shutdown()
    }

    private fun seedSubscription(db: Database, id: String, title: String) {
        db.podcastsQueries.upsert(
            id, "link", title, "description", "email", "imageUrl",
            0L, id, 0L, -1L
        )
    }

    private fun feedXml(title: String, guid: String): String = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
          <channel>
            <title>$title</title>
            <link>https://example.com/$guid</link>
            <description>Description for $title</description>
            <item>
              <guid>$guid</guid>
              <title>Episode for $title</title>
              <link>https://example.com/$guid/ep1</link>
              <pubDate>Mon, 01 Jan 2024 00:00:00 GMT</pubDate>
            </item>
          </channel>
        </rss>
    """.trimIndent()

    @Test
    fun `updateAll fetches every subscription concurrently and upserts each one`() = withDatabase { db ->
        val urlOne = server.url("/feed-one").toString()
        val urlTwo = server.url("/feed-two").toString()
        seedSubscription(db, urlOne, "Feed One (stale)")
        seedSubscription(db, urlTwo, "Feed Two (stale)")

        server.enqueue(MockResponse().setBody(feedXml("Feed One Updated", "feed-one-ep")))
        server.enqueue(MockResponse().setBody(feedXml("Feed Two Updated", "feed-two-ep")))

        val repo = podcastRepo(TestRssHandler())
        val updater = SubscriptionUpdater(client, repo, TestCoroutines.dispatcher)

        val result = updater.updateAll()

        assertEquals(SyncResult(total = 2, succeeded = 2), result)
        val podcasts = repo.getPodcasts().associateBy { it.id }
        assertEquals("Feed One Updated", podcasts[urlOne]!!.title)
        assertEquals("Feed Two Updated", podcasts[urlTwo]!!.title)
    }

    @Test
    fun `updateAll isolates a failing feed so other feeds still complete`() = withDatabase { db ->
        val urlOk = server.url("/feed-ok").toString()
        val urlDown = server.url("/feed-down").toString()
        seedSubscription(db, urlOk, "Feed Ok (stale)")
        seedSubscription(db, urlDown, "Feed Down (stale)")

        server.enqueue(MockResponse().setBody(feedXml("Feed Ok Updated", "feed-ok-ep")))
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val repo = podcastRepo(TestRssHandler())
        val updater = SubscriptionUpdater(client, repo, TestCoroutines.dispatcher)

        val result = updater.updateAll()

        assertEquals(2, result.total)
        assertEquals(1, result.succeeded)
        val podcasts = repo.getPodcasts().associateBy { it.id }
        assertEquals("Feed Ok Updated", podcasts[urlOk]!!.title)
        // The failed feed's row is untouched rather than corrupted or half-written.
        assertEquals("Feed Down (stale)", podcasts[urlDown]!!.title)
    }

    @Test
    fun `updateAll with no subscriptions returns an empty result without failing`() = withDatabase { db ->
        val repo = podcastRepo(TestRssHandler())
        val updater = SubscriptionUpdater(client, repo, TestCoroutines.dispatcher)

        val result = updater.updateAll()

        assertEquals(SyncResult(total = 0, succeeded = 0), result)
    }
}
