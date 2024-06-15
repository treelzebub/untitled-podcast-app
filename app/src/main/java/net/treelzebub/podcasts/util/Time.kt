package net.treelzebub.podcasts.util

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Time {

    private const val DATE_UNKNOWN = "Unknown Date"
    private val locale: Locale
        get() = Locale.getDefault()
    private val zoneClock: Clock
        get() = Clock.systemDefaultZone()
    private val epochStart = LocalDateTime.of(1970, 1, 1, 12, 1)

    // Parsing formats
    private val rfc2822Lenient = DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm:ss[ z]")
    private val rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME

    private val displayFormat = DateTimeFormatter.ofPattern("MMM d, yyyy", locale)

    fun nowSeconds(): Long = System.currentTimeMillis() / 1000L

    fun zonedEpochSeconds(string: String?): Long =
        string?.let {
            parse(string).atZone(zoneClock.zone).toEpochSecond()
        } ?: nowSeconds()

    fun displayFormat(string: String?): String {
        return try {
            string?.let { parse(string).format(displayFormat) }.orEmpty()
        } catch (e: Exception) {
            Logger.e("Error parsing in displayFormat(String)", e)
            DATE_UNKNOWN
        }
    }

    fun displayFormat(seconds: Long): String = format(seconds, displayFormat)

    fun verboseFormat(seconds: Long): String = format(seconds, rfc2822Lenient)

    private fun format(seconds: Long, formatter: DateTimeFormatter): String {
        val zoned = Instant.ofEpochSecond(seconds).atZone(zoneClock.zone)
        return try {
            zoned.format(formatter)
        } catch (e: Exception) {
            Logger.e("Error parsing $seconds with ${formatter::class.java.simpleName})", e)
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
                Logger.e("parse() fell through all tries", e)
                epochStart
            }
        }
    }
}
