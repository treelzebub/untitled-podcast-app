package net.treelzebub.podcasts.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.db.TestCoroutines.dispatcher
import java.util.Properties

object TestCoroutines {
    val scheduler = TestCoroutineScheduler()
    val dispatcher = TestCoroutineDispatcher(scheduler)
    val scope = TestScope(dispatcher)
}

fun createDriver() {
    val driver = JdbcSqliteDriver(
        url = JdbcSqliteDriver.IN_MEMORY,
        properties = Properties().apply { put("foreign_keys", "true") }
    )
    Database.Schema.create(driver)
    TestDb.setUp(driver)
}

fun closeDriver() = TestDb.clear()

fun getDb(): Database = TestDb.instance

fun withDatabase(fn: suspend CoroutineScope.(Database) -> Unit) = runTest(dispatcher) {
    createDriver()
    fn(getDb())
    closeDriver()
}

private object TestDb {
    private var _driver: SqlDriver? = null
    private var _instance: Database? = null

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

    val instance: Database
        get() = _instance!!
}
