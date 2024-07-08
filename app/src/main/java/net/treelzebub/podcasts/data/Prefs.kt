package net.treelzebub.podcasts.data

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.util.Time
import javax.inject.Inject


sealed interface PodcastPref<T> {
    val key: Preferences.Key<T>
    val default: T

    data object LastSyncTimestamp : PodcastPref<Long> {
        override val key = longPreferencesKey("last-sync-timestamp")
        override val default: Long get() = Time.nowSeconds()
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "podcast-preferences")
class Prefs @Inject constructor(
    app: Application,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) {
    private val dataStore = app.dataStore
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun <T> collect(
        pref: PodcastPref<T>,
        default: T = pref.default,
        scope: CoroutineScope = this.scope,
        collector: FlowCollector<T>
    ): Job {
        return scope.launch {
            dataStore.data.collect { prefs ->
                val current = prefs[pref.key]
                if (current == null) {
                    val editedPrefs = edit(pref, default).await()
                    collector.emit(editedPrefs[pref.key]!!)
                } else collector.emit(current)
            }
        }
    }

    fun <T> edit(pref: PodcastPref<T>, value: T): Deferred<Preferences> {
        return scope.async {
            dataStore.edit { prefs -> prefs[pref.key] = value }
        }
    }

    fun dispose() = scope.cancel()
}
