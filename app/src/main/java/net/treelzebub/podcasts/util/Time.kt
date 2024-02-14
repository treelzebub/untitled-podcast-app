package net.treelzebub.podcasts.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.TimeZone

object Time {
    private val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
    private val displayFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    fun nowTimestamp(): String = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(formatter)
    fun displayFormat(string: String?): String = string?.let { LocalDateTime.parse(string, formatter).format(displayFormat) }.orEmpty()
}