package net.treelzebub.podcasts.util

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

object Time {

    private const val DATE_UNKNOWN = "Unknown Date"
    private val locale: Locale
        get() = Locale.getDefault()
    private val zoneClock: Clock
        get() = Clock.systemDefaultZone()
    private val epochStart = LocalDateTime.of(1970, 1, 1, 12, 1)

    // Parsing formats
    private val rfc2822Lenient = DateTimeFormatter.ofPattern("E, d MMM yyyy H:m:s[ z]")
    private val rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME

    private val displayFormat = DateTimeFormatter.ofPattern("MMM d, yyyy", locale)

    fun nowEpochMillis(): Long = System.currentTimeMillis()

    fun zonedEpochMillis(string: String?): Long =
        string?.let {
            parse(string).atZone(zoneClock.zone).toEpochSecond() * 1000
        } ?: nowEpochMillis()

    fun displayFormat(string: String?): String {
        return try {
            string?.let { parse(string).format(displayFormat) }.orEmpty()
        } catch (e: Exception) {
            Log.e("Time", "Error parsing in displayFormat(String)", e)
            DATE_UNKNOWN
        }
    }

    fun displayFormat(millis: Long): String {
        val zoned = Instant.ofEpochMilli(millis).atZone(zoneClock.zone)
        return try {
            zoned.format(displayFormat)
        } catch (e: Exception) {
            Log.e("Time", "Error parsing in displayFormat(Long)", e)
            DATE_UNKNOWN
        }
    }

    private fun parse(string: String?): LocalDateTime {
        return try {
            LocalDateTime.parse(string, rfc2822Lenient)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(string, rfc1123)
            } catch (e: Exception) {
                Log.e("Time", "parse() fell through all tries.", e)
                epochStart
            }
        }
    }
}
