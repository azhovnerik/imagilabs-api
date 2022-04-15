package com.anahoret.imagilabsapi.utils

import java.time.Instant
import java.time.ZonedDateTime

object DateUtils {

    fun toStockholmDateTime(epochMillis: Long): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), TimeZones.EUROPE_STOCKHOLM.toZoneId())
    }

}
