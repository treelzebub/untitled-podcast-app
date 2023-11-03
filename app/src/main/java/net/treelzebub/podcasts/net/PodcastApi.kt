package net.treelzebub.podcasts.net

import net.treelzebub.podcasts.net.models.Feed

interface PodcastApi {

    fun searchByTerm(term: String): List<Feed>

}