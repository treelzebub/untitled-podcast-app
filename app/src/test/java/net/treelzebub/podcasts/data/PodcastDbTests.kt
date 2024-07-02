package net.treelzebub.podcasts.data

import app.cash.turbine.test
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.data.PodcastsRepo.Companion.podcastMapper
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.TestCoroutines
import net.treelzebub.podcasts.util.injectMockData
import net.treelzebub.podcasts.util.podcastRepo
import net.treelzebub.podcasts.util.upsert
import net.treelzebub.podcasts.util.withDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PodcastDbTests {

    @Before fun setUp() {
        Dispatchers.setMain(TestCoroutines.dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun sanity() = withDatabase { db ->
        db.podcastsQueries.upsert(
            "podcastId",
            "link",
            "title",
            "description",
            "email",
            "imageUrl",
            12L,
            "rssLink",
            145L,
            1000000000L
        )
        db.episodesQueries.upsert(
            "id",
            "podcastId",
            "Podcast Title",
            "Episode Title",
            "description",
            1000L,
            "link",
            "streamingLink",
            "localLink",
            "imageUrl",
            "duration",
            false,
            0,
            false,
            false
        )

        val podcasts = db.podcastsQueries.get_all().executeAsList()
        assertEquals(1, podcasts.size)
        assertEquals(12L, podcasts.first().last_build_date)

        val episodes = db.episodesQueries.get_all().executeAsList()
        assertEquals(1, episodes.size)
        assertEquals(1000L, episodes.first().date)
    }

    // TODO update with Hilt best practices
    @Test fun `Sort podcasts by latest episode date`() = withDatabase { db ->
        injectMockData(db)
        val repo = podcastRepo()
        repo.getPodcastsByLatestEpisode().test {
            assertEquals("podcast_02", awaitItem().first().id)
        }
    }

    @Test fun upsert() = withDatabase { db ->
        injectMockData(db)
        val podcast = db.podcastsQueries.get_by_id("podcast_01", podcastMapper).executeAsOne()
        val other = podcast.copy(link = "otherLink", title = "otherTitle", description = "otherDescription", lastLocalUpdate = 43562436L)

        db.podcastsQueries.upsert(other)

        val updated = db.podcastsQueries.get_by_id("podcast_01", podcastMapper).executeAsOne()
        assertEquals(other, updated)
    }
}
