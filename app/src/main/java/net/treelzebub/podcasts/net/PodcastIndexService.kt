package net.treelzebub.podcasts.net

import com.squareup.moshi.JsonClass
import net.treelzebub.podcasts.net.models.Feed
import retrofit2.http.GET
import retrofit2.http.Query

interface Response {
    val status: Int
    val error: String?
}

interface PodcastIndexService {

    /**
     * This call returns all of the feeds that match the search terms in the title, author or owner of the feed.
     */
    @GET("/search/byterm?")
    suspend fun searchPodcasts(@Query("q") query: String): SearchPodcastsResponse
}

@JsonClass(generateAdapter = true)
data class SearchPodcastsResponse(
//    override val status: Int,
//    override val error: String?,
    val count: Int,
    val query: String,
    val description: String,
    val feeds: List<Feed>
)// : Response

