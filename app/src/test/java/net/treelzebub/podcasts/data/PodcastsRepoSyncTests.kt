package net.treelzebub.podcasts.data

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.util.Pod01
import net.treelzebub.podcasts.util.Pod01_Ep01
import net.treelzebub.podcasts.util.Pod01_Ep02
import net.treelzebub.podcasts.util.Pod01_Ep03
import net.treelzebub.podcasts.util.Pod02
import net.treelzebub.podcasts.util.Pod02_Ep01
import net.treelzebub.podcasts.util.Pod02_Ep02
import net.treelzebub.podcasts.util.Pod02_Ep03
import net.treelzebub.podcasts.util.TestCoroutines
import net.treelzebub.podcasts.util.injectMockData
import net.treelzebub.podcasts.util.podcastRepo
import net.treelzebub.podcasts.util.withDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlinx.coroutines.Dispatchers

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PodcastsRepoSyncTests {

    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutines.dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun podcast(id: String, title: String, latest: Long) = Podcast(
        id = id, link = "link", title = title, description = "description", email = "email",
        image_url = "imageUrl", last_build_date = 0L, rss_link = id, last_local_update = 0L,
        latest_episode_timestamp = latest
    )

    private fun episode(id: String, podcastId: String, podcastTitle: String, date: Long) = Episode(
        id = id, podcast_id = podcastId, podcast_title = podcastTitle, title = "title",
        description = "description", date = date, link = "link", streaming_link = "streamingLink",
        local_file_uri = null, image_url = null, duration = "1h", has_played = false,
        position_millis = 0L, is_bookmarked = false, is_archived = false
    )

    @Test
    fun `syncSubscriptions settles the podcast list in a single emission`() = withDatabase {
        val repo = podcastRepo()
        val podcastOne = podcast("podcast-one", "Podcast One", latest = 100L)
        val podcastTwo = podcast("podcast-two", "Podcast Two", latest = 200L)
        val episodesOne = listOf(episode("one-ep1", podcastOne.id, podcastOne.title, 100L))
        val episodesTwo = listOf(episode("two-ep1", podcastTwo.id, podcastTwo.title, 200L))

        repo.getPodcastUis().test {
            assertEquals(emptyList<String>(), awaitItem().map { it.id })

            repo.syncSubscriptions(listOf(podcastOne to episodesOne, podcastTwo to episodesTwo))

            val settled = awaitItem()
            assertEquals(setOf(podcastOne.id, podcastTwo.id), settled.map { it.id }.toSet())
            // The whole sync landed in one transaction: no intermediate, partially-synced emission.
            expectNoEvents()
        }
    }

    @Test
    fun `syncSubscriptions recomputes latest_episode_timestamp from unplayed episodes only`() = withDatabase {
        val repo = podcastRepo()
        val podcast = podcast("podcast-one", "Podcast One", latest = -1L)
        // The newest episode (date=300) is already played, so the recomputed timestamp
        // should reflect the newest *unplayed* one (date=200), not the raw feed max.
        val episodes = listOf(
            episode("ep1", podcast.id, podcast.title, 100L),
            episode("ep2", podcast.id, podcast.title, 200L),
            episode("ep3", podcast.id, podcast.title, 300L).copy(has_played = true)
        )

        repo.syncSubscriptions(listOf(podcast to episodes))

        val updated = repo.getPodcasts().first { it.id == podcast.id }
        assertEquals(200L, updated.latest_episode_timestamp)
    }

    @Test
    fun `refreshLatestEpisodeTimestamps recomputes for existing podcasts outside of a sync`() = withDatabase { db ->
        injectMockData(db)
        val repo = podcastRepo()
        repo.markPlayed(Pod01_Ep01.id, hasPlayed = true)

        repo.refreshLatestEpisodeTimestamps()

        val podcasts = repo.getPodcasts().associateBy { it.id }
        assertEquals(Pod01_Ep03.sortDate, podcasts[Pod01.id]!!.latest_episode_timestamp)
        assertEquals(Pod02_Ep03.sortDate, podcasts[Pod02.id]!!.latest_episode_timestamp)
    }

    @Test
    fun `refreshLatestEpisodeTimestamps sets MIN_VALUE when every episode is played`() = withDatabase { db ->
        injectMockData(db)
        val repo = podcastRepo()
        repo.markPlayed(Pod02_Ep01.id, hasPlayed = true)
        repo.markPlayed(Pod02_Ep02.id, hasPlayed = true)
        repo.markPlayed(Pod02_Ep03.id, hasPlayed = true)

        repo.refreshLatestEpisodeTimestamps()

        val podcasts = repo.getPodcasts().associateBy { it.id }
        assertEquals(Long.MIN_VALUE, podcasts[Pod02.id]!!.latest_episode_timestamp)
        assertEquals(Pod01_Ep01.sortDate, podcasts[Pod01.id]!!.latest_episode_timestamp)
    }
}
