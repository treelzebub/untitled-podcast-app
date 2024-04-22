package net.treelzebub.podcasts.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.util.IoDispatcher
import net.treelzebub.podcasts.util.Log
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Settings @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @IoDispatcher
    private val coroutineScope: CoroutineScope
) {
    private val Context.dataStore by preferencesDataStore(name = "podcasts-data-store")

    fun <T> edit(setting: Setting<T>) {
        launch {
            context.dataStore.edit { settings ->
                when (val value = setting.value) {
                    is Boolean -> {
                        val key = booleanPreferencesKey(setting.key)
                        settings[key] = value
                    }
                    is Int -> {
                        val key = intPreferencesKey(setting.key)
                        settings[key] = value
                    }
                    else -> throw IllegalArgumentException("Preference ${setting.key} is of unsupported type.")
                }
            }
        }
        Log.d("Settings", "${setting.key} changed to ${setting.value}")
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(setting: Setting<T>): Flow<T> {
        val key = when (setting.value) {
            is Boolean -> booleanPreferencesKey(setting.key)
            is Int -> intPreferencesKey(setting.key)
            else -> throw IllegalArgumentException("Preference ${setting.key} is of unsupported type.")
        }
        return context.dataStore.data.map { it[key] as T }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) = coroutineScope.launch(block = block)
}
