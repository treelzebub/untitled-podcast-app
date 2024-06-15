package net.treelzebub.podcasts.util

import net.treelzebub.podcasts.ui.models.EpisodeUi

object PreviewMocks {

    val Episode = EpisodeUi(
        id = "id",
        channelId = "podcast id",
        channelTitle = "Podcast Name, but it might be long so let's test ellipsizing...",
        title = "Episode Title: How Booping Snoots Heals All Wounds, an In-Depth Analysis",
        description = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend felis a eleifend placerat. Pellentesque eget euismod elit. Pellentesque in lorem odio. Nunc non quam sit amet erat eleifend tincidunt. Donec aliquam nulla nec diam pulvinar imperdiet suscipit in ex. Curabitur id tellus nunc. Etiam vitae nibh volutpat massa cursus convallis eget vitae eros. Phasellus sit amet imperdiet augue. Praesent tempus sed mi et scelerisque. Etiam tellus metus, ultrices non efficitur ac, tristique sed quam. Duis tincidunt feugiat magna sed rutrum. Proin nec libero at mauris tristique consequat. Sed quis felis ut magna sagittis aliquet. Phasellus ullamcorper urna et aliquet congue. Vestibulum efficitur quis felis et accumsan. 
        """.trimIndent(),
        displayDate = Time.displayFormat(1704096060L),
        sortDate = 1704096060L,
        link = "https://podcast.home/link",
        streamingLink = "https://podcast.home/stream.mp3",
        imageUrl = "https://picsum.photos/200",
        duration = "5h 30m",
        hasPlayed = false,
        progressSeconds = 0,
        isBookmarked = false,
        isArchived = false
    )
}
