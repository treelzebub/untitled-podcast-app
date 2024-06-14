package net.treelzebub.podcasts.db

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.net.PodcastRssHandler
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.util.StubRssHandler
import net.treelzebub.podcasts.util.Time
import org.junit.Test
import kotlin.test.assertEquals

class PodcastDbTests {
    private val testScheduler = TestCoroutineScheduler()

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
        val repo = PodcastsRepo(StubRssHandler(), db, StandardTestDispatcher(testScheduler))
        repo.
    }

    private val TestEpisode = Episode(
        id = "id",
        channel_id = "podcast id",
        channel_title = "Podcast Name, but it might be long so let's test ellipsizing...",
        title = "Episode Title: How Booping Snoots Heals All Wounds, an In-Depth Analysis",
        description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
        date = 0L,
        link = "https://podcast.home/link",
        streaming_link = "https://podcast.home/stream.mp3",
        image_url = "https://picsum.photos/200",
        duration = "5h 30m",
        has_played = false,
        progress_seconds = 0,
        is_bookmarked = false,
        is_archived = false
    )

    private val TestEpisodeUi = EpisodeUi(
        id = "id",
        channelId = "podcast id",
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
        "id",
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
