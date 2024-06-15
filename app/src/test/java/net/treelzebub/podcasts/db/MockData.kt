package net.treelzebub.podcasts.db

import net.treelzebub.podcasts.Podcast
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.util.Time

/**
 *
 * 1718411533000L // Fri Jun 14 17:32:13 GMT-07:00 DST
 *
 */

private val TestEpisode = EpisodeUi(
    id = "episode_1",
    channelId = "podcast_1",
    channelTitle = "Podcast Name, but it might be long so let's test ellipsizing...",
    title = "Episode Title: How Booping Snoots Heals All Wounds, an In-Depth Analysis",
    description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
    sortDate = 1704096060000L, // Mon, Jan 1, 2024 00:01:00 GMT,
    displayDate = Time.displayFormat(1704096060L),
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