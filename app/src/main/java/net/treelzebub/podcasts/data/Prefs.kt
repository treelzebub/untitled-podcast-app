package net.treelzebub.podcasts.data

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject


sealed interface PodcastPref<T : Any> {
    val key: String
    val default: T

    data object LastSyncTimestamp : PodcastPref<Long> {
        override val key = "last-sync-timestamp"
        override val default: Long = -1L
    }

    class EpisodesShowPlayed(val podcastId: String) : PodcastPref<Boolean> {
        override val key: String = "$podcastId-episodes-show-played"
        override val default: Boolean = false
    }
}

class Prefs @Inject constructor(app: Application) {
    private val prefs = app.getSharedPreferences("podcast-prefs", MODE_PRIVATE)
    private val editor get() = prefs.edit()

    fun getBoolean(pref: PodcastPref<Boolean>): Boolean = prefs.getBoolean(pref.key, pref.default)
    fun getFloat(pref: PodcastPref<Float>): Float = prefs.getFloat(pref.key, pref.default)
    fun getInt(pref: PodcastPref<Int>): Int = prefs.getInt(pref.key, pref.default)
    fun getLong(pref: PodcastPref<Long>): Long = prefs.getLong(pref.key, pref.default)
    fun getString(pref: PodcastPref<String>): String = prefs.getString(pref.key, pref.default)!!

    fun putBoolean(pref: PodcastPref<Boolean>, value: Boolean) = editor.putBoolean(pref.key, value).apply()
    fun putFloat(pref: PodcastPref<Float>, value: Float) = editor.putFloat(pref.key, value).apply()
    fun putInt(pref: PodcastPref<Int>, value: Int) = editor.putInt(pref.key, value).apply()
    fun putLong(pref: PodcastPref<Long>, value: Long) = editor.putLong(pref.key, value).apply()
    fun putString(pref: PodcastPref<String>, value: String) = editor.putString(pref.key, value).apply()

    fun booleanFlow(pref: PodcastPref<Boolean>): Flow<Boolean> {
        return callbackFlow {
            val listener = OnSharedPreferenceChangeListener { _, it ->
                if (it == pref.key) trySend(getBoolean(pref)); Timber.d("${pref.key} changed to ${getBoolean(pref)}")
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            trySend(getBoolean(pref))

            awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }.buffer(Channel.UNLIMITED)
    }
}
