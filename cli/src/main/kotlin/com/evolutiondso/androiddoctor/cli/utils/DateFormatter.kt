package com.evolutiondso.androiddoctor.cli.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFormatter {

    private val prettyFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy — HH:mm")
        .withZone(ZoneId.systemDefault())

    fun pretty(iso: String?): String {
        if (iso.isNullOrBlank()) return "<unknown>"

        return try {
            val instant = Instant.parse(iso)
            prettyFormatter.format(instant)
        } catch (e: Exception) {
            iso
        }
    }
}
