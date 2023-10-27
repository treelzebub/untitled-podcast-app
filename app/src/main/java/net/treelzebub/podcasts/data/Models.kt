package net.treelzebub.podcasts.data


data class Image(
    val url: String,
    val title: String,
    val link: String
)

data class Enclosure(
    val length: String, // coerce to Long
    val type: String, // MIME. Probably "audio/mpeg"
    val url: String
)
data class Episode(
    val title: String,
    val link: String,
    val description: String, // HTML with escaped symbols
    val guid: String, // probably a Long
    val pubDate: String, // Mon, 29 May 2023 21:18:27 GMT
    val enclosure: Enclosure
) {
    val length: String
        get() = enclosure.length

    val type: String
        get() = enclosure.type

    val url: String
        get() = enclosure.url
}

data class Channel(
    val title: String,
    val link: String,
    val description: String,
    val selfLink: String, // the link to the RSS file we're currently looking at
    val language: String, // locale values
    val pubDate: String, // Wed, 07 Jun 2023 23:08:20 GMT
    val lastBuildDate: String,
    val image: Image,
    val episodes: List<Episode>
)
