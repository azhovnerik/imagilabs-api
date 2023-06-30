package com.anahoret.imagilabsapi.utils

import java.time.Instant
import java.time.ZonedDateTime

object DateUtils {

    const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    fun toStockholmDateTime(epochMillis: Long): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), TimeZones.EUROPE_STOCKHOLM.toZoneId())
    }

}
