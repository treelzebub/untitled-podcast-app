package net.treelzebub.podcasts.util

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TimeTests {

    @Test fun timestampParsing() {
        val withGmt = "Fri, 27 Oct 2023 21:17:18 GMT"
        val withUsZone = "Fri, 27 Oct 2023 21:17:18 PDT"
        val withoutZone = "Fri, 1 Mar 2013 1:17:18"
        val withGmtSingleDay = "Wed, 8 Nov 2023 19:53:52 GMT"

        val formatter = DateTimeFormatter.ofPattern("E, d MMM yyyy H:m:s[ z]")

        val parsedWithGmt = LocalDateTime.parse(withGmt, formatter)
        val parsedWithUsZone = ZonedDateTime.parse(withUsZone, formatter)
        val parsedWithoutZone = LocalDateTime.parse(withoutZone, formatter)
        val parsedWithGmtSingleDay = LocalDateTime.parse(withGmtSingleDay, formatter)

        assertEquals(27, parsedWithGmt.dayOfMonth)
        assertEquals("America/Los_Angeles", parsedWithUsZone.zone.id)
        assertEquals("March", parsedWithoutZone.month.name.lowercase().capitalize(Locale.US))
        assertEquals(8, parsedWithGmtSingleDay.dayOfMonth)

        assertFails { formatter.parse("Fri, 27 Oct 2023 21:17:18 QDO") }
    }
}
