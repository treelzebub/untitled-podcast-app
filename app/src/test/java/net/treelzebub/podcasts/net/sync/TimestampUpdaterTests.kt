package net.treelzebub.podcasts.net.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
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
class TimestampUpdaterTests {

    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutines.dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `update recomputes latest_episode_timestamp from unplayed episodes only`() = withDatabase { db ->
        injectMockData(db)
        val repo = podcastRepo()
        // Pod01's newest episode (Ep01, the max date) is marked played, so the newest
        // *unplayed* episode (Ep03) should become its new latest_episode_timestamp.
        repo.markPlayed(Pod01_Ep01.id, hasPlayed = true)
        val updater = TimestampUpdater(TestCoroutines.dispatcher, repo)

        updater.update()

        val podcasts = repo.getPodcasts().associateBy { it.id }
        assertEquals(Pod01_Ep03.sortDate, podcasts[Pod01.id]!!.latest_episode_timestamp)
        // Pod02 is untouched, so its latest timestamp should still reflect its newest episode.
        assertEquals(Pod02_Ep03.sortDate, podcasts[Pod02.id]!!.latest_episode_timestamp)
    }

    @Test
    fun `update sets latest_episode_timestamp to MIN_VALUE when every episode is played`() = withDatabase { db ->
        injectMockData(db)
        val repo = podcastRepo()
        repo.markPlayed(Pod02_Ep01.id, hasPlayed = true)
        repo.markPlayed(Pod02_Ep02.id, hasPlayed = true)
        repo.markPlayed(Pod02_Ep03.id, hasPlayed = true)
        val updater = TimestampUpdater(TestCoroutines.dispatcher, repo)

        updater.update()

        val podcasts = repo.getPodcasts().associateBy { it.id }
        assertEquals(Long.MIN_VALUE, podcasts[Pod02.id]!!.latest_episode_timestamp)
        assertEquals(Pod01_Ep01.sortDate, podcasts[Pod01.id]!!.latest_episode_timestamp)
    }
}
