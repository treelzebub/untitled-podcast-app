package net.treelzebub.podcasts.util

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import net.treelzebub.podcasts.App
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.data.MoshiSerializer
import net.treelzebub.podcasts.data.PodcastQueue
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.QueueStore
import java.util.Properties


fun podcastRepo(): PodcastsRepo {
    return PodcastsRepo(StubRssHandler(), getDb(), queueStore(), TestCoroutines.dispatcher)
}

fun queueStore(): QueueStore {
    return QueueStore(
        getApplicationContext<App>(),
        MoshiSerializer(PodcastQueue::class.java),
        TestCoroutines.dispatcher
    )
}

fun withQueueStore(fn: suspend CoroutineScope.(QueueStore) -> Unit) = runTest(TestCoroutines.dispatcher) {
    fn(queueStore())
}

fun getDb(): Database = TestDb.instance

fun withDatabase(fn: suspend CoroutineScope.(Database) -> Unit) = runTest(TestCoroutines.dispatcher) {
    createDriver()
    fn(getDb())
    closeDriver()
}

private fun createDriver() {
    val driver = JdbcSqliteDriver(
        url = JdbcSqliteDriver.IN_MEMORY,
        properties = Properties().apply { put("foreign_keys", "true") }
    )
    Database.Schema.create(driver)
    TestDb.setUp(driver)
}

private fun closeDriver() = TestDb.clear()

private object TestDb {
    private var _driver: SqlDriver? = null
    private var _instance: Database? = null

    val instance: Database
        get() = _instance!!

    fun setUp(driver: SqlDriver) {
        val db = Database(driver)
        _driver = driver
        _instance = db
    }

    fun clear() {
        _driver!!.close()
        _instance = null
        _driver = null
    }
}