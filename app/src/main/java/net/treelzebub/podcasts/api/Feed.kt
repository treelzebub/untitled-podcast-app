package net.treelzebub.podcasts.api

data class SearchByTermResponse(
    val status: Boolean,
    val feeds: List<Feed>,
    val count: Int,
    val query: String,
    val description: String
)

data class Feed(
    val id: Long, // api type integer
    val podcastGuid: String,
    val title: String,
    val url: String,
    val originalUrl: String,
    val link: String,
    val description: String,
    val author: String,
    val ownerName: String,
    val image: String,
    val artwork: String,
    val lastUpdateTime: Long, // api type integer
    val lastCrawlTime: Long, // api type integer
    val lastParseTime: Long, // api type integer
    val lastGoodHttpStatusTime: Long, // api type integer
    val lastHttpStatus: Int,
    val contentType: String,
    val itunesId: Long?, // api type integer
    val generator: String,
    val language: String,
    val explicit: Boolean,
    val type: Int, // 0: RSS, 1: Atom
    val medium: String, // https://github.com/Podcastindex-org/podcast-namespace/blob/main/docs/1.0.md#medium
    val dead: Int, // Boolean. does the API consider this podcast dead?
    val episodeCount: Int,
    val crawlErrors: Long, // api type integer
    val parseError: Long, // api type integer
    // val categories: Map<String, String> // Category ID to Name. Values returned by categories/list API.
    val locked: Int, // Boolean. Is an outside app like mine allowed to import this feed?
    val imageUrlHash: Long, // api type integer
    val newestItemPubdate: Long, // api type integer
)