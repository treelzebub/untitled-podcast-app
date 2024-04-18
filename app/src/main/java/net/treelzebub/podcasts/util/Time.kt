package net.treelzebub.podcasts.util

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Time {

    private val locale: Locale
        get() = Locale.getDefault()

    // Parsing formats
    private val rfc2822Lenient = DateTimeFormatter.ofPattern("E, d MMM yyyy H:m:s[ z]", locale)
    private val rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME

    private val displayFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    fun nowTimestamp(): String = ZonedDateTime.now().format(rfc1123)
    fun nowEpochMillis(): Long = ZonedDateTime.now().toEpochSecond()
    fun displayFormat(string: String?): String =
        string?.let { parse(string).format(displayFormat) }.orEmpty()

    private fun parse(string: String?): LocalDateTime {
        return try {
            LocalDateTime.parse(string, rfc2822Lenient)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(string, rfc1123)
            } catch (e: Exception) {
                Log.e("Time", "parse() fell through all tries.", e)
                LocalDateTime.of(1970, 1, 1, 12, 1)
            }
        }
    }
}
