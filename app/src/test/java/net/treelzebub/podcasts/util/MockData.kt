package net.treelzebub.podcasts.util

import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi


fun injectMockData(db: Database) {
    db.podcastsQueries.upsert(Pod01, Pod02)
    db.episodesQueries.upsert(Pod01_Ep01, Pod01_Ep02, Pod01_Ep03, Pod02_Ep01, Pod02_Ep02, Pod02_Ep03)
}

/**
 * 1718411533 // Friday, June 14, 2024 5:32:13 PM  *LATEST!
 * 1623717133 // Tuesday, June 15, 2021 12:32:13 AM
 * 1592181133 // Monday, June 15, 2020 12:32:13 AM
 *
 * 1715733133 // Wednesday, May 15, 2024 12:32:13 AM
 * 1621038733 // Saturday, May 15, 2021 12:32:13 AM
 * 1589502733 // Friday, May 15, 2020 12:32:13 AM
 */

private val Pod01_Ep01 = EpisodeUi(
    id = "pod01_ep01",
    podcastId = "podcast_01",
    podcastTitle = "Podcast the First",
    title = "How Booping Snoots Heals All Wounds, an In-Depth Analysis",
    description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
    sortDate = 1715733133,
    displayDate = Time.displayFormat(1715733133),
    link = "https://podcast_01.home/link",
    streamingLink = "https://podcast.home/stream.mp3",
    imageUrl = "https://picsum.photos/200",
    duration = "5h 30m",
    hasPlayed = false,
    progressSeconds = 0,
    isBookmarked = false,
    isArchived = false
)

private val Pod01_Ep02 = EpisodeUi(
    id = "pod01_ep02",
    podcastId = "podcast_01",
    podcastTitle = "Hey, look! It's episode two!",
    title = "Anton Sleeping in the Sun: A Meditation",
    description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
    sortDate = 1589502733,
    displayDate = Time.displayFormat(1589502733),
    link = "https://podcast_01.home/link",
    streamingLink = "https://podcast.home/stream.mp3",
    imageUrl = "https://picsum.photos/200",
    duration = "5h 30m",
    hasPlayed = false,
    progressSeconds = 0,
    isBookmarked = false,
    isArchived = false
)

private val Pod01_Ep03 = EpisodeUi(
    id = "pod01_ep03",
    podcastId = "podcast_01",
    podcastTitle = "Podcast Name, but it might be long so let's test ellipsizing...",
    title = "Episode Title: How Booping Snoots Heals All Wounds, an In-Depth Analysis",
    description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
    sortDate = 1621038733,
    displayDate = Time.displayFormat(1621038733),
    link = "https://podcast_01.home/link",
    streamingLink = "https://podcast.home/stream.mp3",
    imageUrl = "https://picsum.photos/200",
    duration = "5h 30m",
    hasPlayed = false,
    progressSeconds = 0,
    isBookmarked = false,
    isArchived = false
)

private val Pod01 = PodcastUi(
    "podcast_01",
    "link",
    "title",
    "description",
    "email",
    "imageUrl",
    Pod01_Ep01.displayDate,
    "rssLink",
    145L,
    1715733133L
)


private val Pod02_Ep01 = EpisodeUi(
    id = "pod02_ep01",
    podcastId = "podcast_02",
    podcastTitle = "Podcast Name, but it might be long so let's test ellipsizing...",
    title = "Episode Title: How Booping Snoots Heals All Wounds, an In-Depth Analysis",
    description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
    sortDate = 1623717133,
    displayDate = Time.displayFormat(1623717133),
    link = "https://podcast_02.home/link",
    streamingLink = "https://podcast.home/stream.mp3",
    imageUrl = "https://picsum.photos/200",
    duration = "5h 30m",
    hasPlayed = false,
    progressSeconds = 0,
    isBookmarked = false,
    isArchived = false
)

private val Pod02_Ep02 = EpisodeUi(
    id = "pod02_ep02",
    podcastId = "podcast_02",
    podcastTitle = "Podcast Name, but it might be long so let's test ellipsizing...",
    title = "Episode Title: How Booping Snoots Heals All Wounds, an In-Depth Analysis",
    description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
    sortDate = 1592181133,
    displayDate = Time.displayFormat(1592181133),
    link = "https://podcast_02.home/link",
    streamingLink = "https://podcast.home/stream.mp3",
    imageUrl = "https://picsum.photos/200",
    duration = "5h 30m",
    hasPlayed = false,
    progressSeconds = 0,
    isBookmarked = false,
    isArchived = false
)

private val Pod02_Ep03 = EpisodeUi(
    id = "pod02_ep03",
    podcastId = "podcast_02",
    podcastTitle = "Podcast Name, but it might be long so let's test ellipsizing...",
    title = "Episode Title: How Booping Snoots Heals All Wounds, an In-Depth Analysis",
    description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
            """.trimIndent(),
    sortDate = 1718411533,
    displayDate = Time.displayFormat(1718411533),
    link = "https://podcast_02.home/link",
    streamingLink = "https://podcast.home/stream.mp3",
    imageUrl = "https://picsum.photos/200",
    duration = "5h 30m",
    hasPlayed = false,
    progressSeconds = 0,
    isBookmarked = false,
    isArchived = false
)

private val Pod02 = PodcastUi(
    "podcast_02",
    "link",
    "title",
    "description",
    "email",
    "imageUrl",
    Pod02_Ep03.displayDate,
    "rssLink",
    145L,
    1718411533L
)
