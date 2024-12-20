package net.treelzebub.podcasts.di

import android.app.Application
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.PodcastsQueries
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDriver(app: Application): SqlDriver =
        AndroidSqliteDriver(
            Database.Schema,
            app,
            "podcasts.db",
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) = db.setForeignKeyConstraintsEnabled(true)
            }
        )

    @Provides
    @Singleton
    fun provideDatabase(driver: SqlDriver): Database = Database(driver)

    @Provides
    fun providePodcastQueries(database: Database): PodcastsQueries = database.podcastsQueries
}