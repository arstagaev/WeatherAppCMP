package com.tagaev.data.utils

import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char

/** Unix epoch milliseconds (UTC). */
expect fun nowEpochMs(): Long

// AMPM to EURO
private val AmPmLocalTimeFormat = LocalTime.Format {
    amPmHour(); char(':'); minute();
    char(' '); amPmMarker(am = "AM", pm = "PM")
}

fun String.toLocalTimeAmPm(): LocalTime = try {
    LocalTime.parse(this.trim(), AmPmLocalTimeFormat)
} catch (_: Throwable) {
    // Fallback for unexpected inputs
    parseAmPmFallback(this.trim()) ?: LocalTime(0, 0)
}

private val AMPM_REGEX = Regex("""^\s*(\n{1,2}|\d{1,2}):(\d{2})\s*([AaPp][Mm])\s*$""")

private fun parseAmPmFallback(raw: String): LocalTime? {
    val m = AMPM_REGEX.matchEntire(raw) ?: return null
    var h = m.groupValues[1].toIntOrNull() ?: return null
    val min = m.groupValues[2].toIntOrNull() ?: return null
    val ap = m.groupValues[3].uppercase()
    if (ap == "AM") {
        if (h == 12) h = 0
    } else {
        if (h in 0..11) h += 12
    }
    return try { LocalTime(hour = h, minute = min) } catch (_: Throwable) { null }
}

private val HHmm = LocalTime.Format { hour(); char(':'); minute() }