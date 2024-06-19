package net.treelzebub.podcasts.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.net.PodcastIndexService
import javax.inject.Inject


class SearchQueriesRepo @Inject constructor(
    private val api: PodcastIndexService,
    private val db: Database,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun searchPodcasts(query: String) = withContext(ioDispatcher) {
        insert(query)
        api.searchPodcasts(query)
    }

    suspend fun all() = withContext(ioDispatcher) {
        db.searchesQueries.select_all().asFlow().mapToList(ioDispatcher)
    }

    suspend fun insert(query: String) = withContext(ioDispatcher) {
        db.searchesQueries.insert(query)
    }

    suspend fun delete(query: String) = withContext(ioDispatcher) {
        db.searchesQueries.delete(query)
    }
}
