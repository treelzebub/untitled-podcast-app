package net.treelzebub.podcasts.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.di.IoDispatcher
import javax.inject.Inject


class SearchQueriesRepo @Inject constructor(
    private val db: Database,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun all() = db.searchesQueries.select_all().asFlow().mapToList(ioDispatcher)
    fun insert(query: String) = db.searchesQueries.insert(query)
    fun delete(query: String) = db.searchesQueries.delete(query)
}