package net.treelzebub.podcasts.db

import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.util.insert_or_replace
import net.treelzebub.podcasts.util.upsert
import org.junit.Test
import kotlin.test.assertEquals

class PodcastDbTests {

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
        listOf(
            TestPodcast.copy(id = "foo", last_build_date = -1),
            TestPodcast.copy(id = "bar", last_build_date = -2)
        ).forEach { db.podcastsQueries.insert_or_replace(it) }
        (0..25L).map { i ->
            TestEpisode.copy(id = "foo-$i", channel_id = "foo", date = i)
                .also { db.episodesQueries.upsert(it) }
        }
        (0..26L).map { i ->
            TestEpisode.copy(id = "bar-$i", channel_id = "bar", date = i*2)
                .also { db.episodesQueries.upsert(it) }
        }

        val unsorted = db.podcastsQueries.get_all().executeAsList()
        val sorted = db.podcastsQueries.get_all_by_latest_episode().executeAsList()

        val latestId = db.episodesQueries.get_all().executeAsList().maxBy { it.date }.channel_id

        val stop = ""

        assertEquals(latestId, sorted.first().id)
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
        date = "Mon, Jan 1, 2024",
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
