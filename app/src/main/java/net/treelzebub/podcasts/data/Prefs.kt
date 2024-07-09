package net.treelzebub.podcasts.data

import android.app.Application
import android.content.Context.MODE_PRIVATE
import javax.inject.Inject


sealed interface PodcastPref<T : Any> {
    val key: String
    val default: T

    data object LastSyncTimestamp : PodcastPref<Long> {
        override val key = "last-sync-timestamp"
        override val default: Long = -1L
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

    fun putBoolean(pref: PodcastPref<Boolean>, value: Boolean) = editor.putBoolean(pref.key, pref.default).apply()
    fun putFloat(pref: PodcastPref<Float>, value: Float) = editor.putFloat(pref.key, pref.default).apply()
    fun putInt(pref: PodcastPref<Int>, value: Int) = editor.putInt(pref.key, pref.default).apply()
    fun putLong(pref: PodcastPref<Long>, value: Long) = editor.putLong(pref.key, pref.default).apply()
    fun putString(pref: PodcastPref<String>, value: String) = editor.putString(pref.key, pref.default).apply()
}
