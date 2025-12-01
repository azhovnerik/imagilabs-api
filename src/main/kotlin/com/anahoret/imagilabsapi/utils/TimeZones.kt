package com.anahoret.imagilabsapi.utils

import java.util.*

object TimeZones {

    const val EUROPE_STOCKHOLM_VALUE = "Europe/Stockholm"
    const val AMERICA_DENVER_VALUE = "America/Denver"
    const val UTC_VALUE = "UTC"
    val EUROPE_STOCKHOLM: TimeZone = TimeZone.getTimeZone(EUROPE_STOCKHOLM_VALUE)
    val UTC: TimeZone = TimeZone.getTimeZone(UTC_VALUE)
}
