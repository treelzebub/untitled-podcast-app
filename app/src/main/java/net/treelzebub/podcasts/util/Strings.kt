package net.treelzebub.podcasts.util

import androidx.core.text.isDigitsOnly
import java.util.Locale

object Strings {

    fun formatDuration(seconds: String?): String {
        val timecodePattern = Regex("""^\d+:[0-9]\d(?::[0-9]\d)?$""")

        if (seconds?.matches(timecodePattern) == true) {
            val parts = seconds.split(":")
            val hours = parts[0].toInt()
            val mins = parts[1].toInt()

            return if (hours == 0) "${mins}m" else "${hours}h ${mins}m"
        }

        if (seconds?.isDigitsOnly() == true) {
            val long = seconds.toLong()
            val mins = long / 60
            val hours = mins / 60
            val secs = mins % 60
            return "" +
                if (hours > 0) "${hours}h" else "" +
                    if (mins > 0) "${mins}m" else "" +
                        "${secs}s"
        }

        return ""
    }

    fun formatPosition(current: Long, total: Long): String {
        val cHours = (current / (1000 * 60 * 60)) % 24
        val cMins = (current / (1000 * 60)) % 60
        val cSecs = (current / 1000) % 60
        val tHours = (total / (1000 * 60 * 60)) % 24
        val tMins = (total / (1000 * 60)) % 60
        val tSecs = (total / 1000) % 60

        return when {
            tHours > 0 -> String.format(
                Locale.getDefault(), "%02d:%02d:%02d / %02d:%02d:%02d",
                cHours, cMins, cSecs,
                tHours, tMins, tSecs
            )

            else -> String.format(
                Locale.getDefault(), "%02d:%02d / %02d:%02d",
                cMins, cSecs, tMins, tSecs
            )
        }
    }

}