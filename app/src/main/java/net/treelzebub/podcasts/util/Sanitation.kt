package net.treelzebub.podcasts.util

import android.text.Html
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.sanitizeUrl(): String = replace("&amp;", "&")
fun String?.sanitizeHtml(): String = this?.let { Html.fromHtml(this).toString() }.orEmpty()
fun String?.orNow(): String = this ?: "TODO()"

fun String.formatDate(): String {
    val localDateTime = LocalDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME)
    val displayFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    return localDateTime.format(displayFormat)
}