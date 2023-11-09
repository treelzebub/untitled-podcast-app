package net.treelzebub.podcasts.di

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.treelzebub.podcasts.Database
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDriver(app: Application): SqlDriver = AndroidSqliteDriver(Database.Schema, app, "podcasts.db")

    @Provides
    @Singleton
    fun provideDatabase(driver: SqlDriver): Database = Database(driver)
}