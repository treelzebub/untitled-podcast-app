package net.treelzebub.podcasts.util

import androidx.core.text.isDigitsOnly

object Strings {

    fun formatDuration(seconds: String?): String {
        val timecodePattern = Regex("""^(?:[01]\d|2[0-3]):[0-5]\d(?::[0-5]\d)?$""")

        if (seconds?.matches(timecodePattern) == true) {
            val parts = seconds.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()

            return if (hours == 0) "${minutes}m" else "${hours}h ${minutes}m"
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
}