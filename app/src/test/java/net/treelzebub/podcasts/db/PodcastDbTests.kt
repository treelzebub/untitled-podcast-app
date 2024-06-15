package net.treelzebub.podcasts.db

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.StubRssHandler
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PodcastDbTests {

    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutines.dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun sanity() = withDatabase { db ->
        db.podcastsQueries.insert_or_replace(
            "podcastId",
            "link",
            "title",
            "description",
            "email",
            "imageUrl",
            12L,
            "rssLink",
            145L
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
            "imageUrl",
            "duration",
        )

        val podcasts = db.podcastsQueries.get_all().executeAsList()
        assertEquals(1, podcasts.size)
        assertEquals(12L, podcasts.first().last_build_date)

        val episodes = db.episodesQueries.get_all().executeAsList()
        assertEquals(1, episodes.size)
        assertEquals(1000L, episodes.first().date)
    }

    @Test fun `Sort podcasts by latest episode date`() = withDatabase { db ->
        injectMockData(db)
        TestCoroutines.scope.launch {
            val repo = PodcastsRepo(StubRssHandler(), db, TestCoroutines.dispatcher)
            var list: List<PodcastUi> = listOf()
            repo.getPodcastsByLatestEpisode().collectLatest { list = it }

            assertEquals("podcast_02", list.first().id)
        }
    }
}
