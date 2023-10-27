package net.treelzebub.podcasts.api

interface PodcastApi {

    fun searchByTerm(term: String): List<Feed>

}