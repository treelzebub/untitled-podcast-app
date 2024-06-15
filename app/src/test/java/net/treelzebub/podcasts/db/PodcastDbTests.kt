package net.treelzebub.podcasts.db

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.util.StubRssHandler
import net.treelzebub.podcasts.util.Time
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
        TestCoroutines.dispatcher.cleanupTestCoroutines()
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
        TestCoroutines.scope.launch {
            val repo = PodcastsRepo(StubRssHandler(), db, TestCoroutines.dispatcher)



            var map: Map<PodcastUi, List<EpisodeUi>> = mapOf()
            repo.getAllPodcastsByLatestEpisode().collectLatest { map = it }

            assert(map.isEmpty())
        }
    }

    private val TestEpisode = EpisodeUi(
        id = "id",
        channelId = "podcast_id",
        channelTitle = "Podcast Name, but it might be long so let's test ellipsizing...",
        title = "Episode Title: How Booping Snoots Heals All Wounds, an In-Depth Analysis",
        description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
        sortDate = 1704096060000L, // Mon, Jan 1, 2024 00:01:00,
        displayDate = Time.displayFormat(1704096060000L),
        link = "https://podcast.home/link",
        streamingLink = "https://podcast.home/stream.mp3",
        imageUrl = "https://picsum.photos/200",
        duration = "5h 30m",
        hasPlayed = false,
        progressSeconds = 0,
        isBookmarked = false,
        isArchived = false
    )

    private val TestPodcast = Podcast(
        "podcast_id",
        "link",
        "title",
        "description",
        "email",
        "imageUrl",
        12L,
        "rssLink",
        145L,
    )
}
