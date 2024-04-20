package net.treelzebub.podcasts.util

import android.text.Html
import android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH

fun String?.sanitizeUrl(): String? {
    return try {
        this?.let { replace("&amp;", "&") }
    } catch (e: Exception) {
        Log.e("sanitizeUrl()", "Parsing error.", e)
        null
    }
}

fun String?.sanitizeHtml(): String? {
    return try {
        this?.let { Html.fromHtml(this, FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString() }
    } catch (e: Exception) {
        Log.e("sanitizeHtml()", "Parsing error.", e)
        this
    }
}
