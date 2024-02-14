package net.treelzebub.podcasts.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import net.treelzebub.podcasts.Database
import javax.inject.Inject


class PreviousSearchRepo @Inject constructor(private val db: Database) {

    fun all() = db.searchesQueries.select_all().asFlow().mapToList(Dispatchers.IO)
    fun insert(query: String) = db.searchesQueries.insert(query)
    fun delete(query: String) = db.searchesQueries.delete(query)
}