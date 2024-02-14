package net.treelzebub.podcasts.util

import android.text.Html
import android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH

fun String?.sanitizeUrl(): String? = this?.let { replace("&amp;", "&") }
fun String?.sanitizeHtml(): String? =
    this?.let { Html.fromHtml(this, FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString() }

fun String?.orNow(): String = this ?: Time.nowTimestamp()
