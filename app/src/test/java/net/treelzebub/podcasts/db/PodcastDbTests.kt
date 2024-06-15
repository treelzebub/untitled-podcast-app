package net.treelzebub.podcasts.db

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
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
        db.podcastsQueries.insert_or_replace("id",
            "link",
            "title",
            "description",
            "email",
            "imageUrl",
            12L,
            "rssLink",
            145L
        )

        val all = db.podcastsQueries.get_all().executeAsList()
        assertEquals(1, all.size)
        assertEquals(12L, all.first().last_build_date)
    }

    @Test fun `Sort podcasts by latest episode date`() = withDatabase { db ->
        injectMockData(db)
        TestCoroutines.scope.launch {
            val repo = PodcastsRepo(StubRssHandler(), db, TestCoroutines.dispatcher)
            var map: Map<PodcastUi, List<EpisodeUi>> = mapOf()
            repo.getAllPodcastsByLatestEpisode().collectLatest { map = it }

            assertEquals("podcast_02", map.entries.first().key.id)
        }
    }
}
