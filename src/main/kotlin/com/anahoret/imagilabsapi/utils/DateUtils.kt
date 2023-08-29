package com.anahoret.imagilabsapi.utils

import java.time.*

object DateUtils {

    const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    fun toStockholmDateTime(epochMillis: Long): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), TimeZones.EUROPE_STOCKHOLM.toZoneId())
    }

    fun toUTC(epochMillis: Long): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of("UTC"))
    }

    fun ZonedDateTime.toMidnight(): ZonedDateTime {
        return ZonedDateTime.of(this.toLocalDate(), LocalTime.MIDNIGHT, ZoneOffset.UTC)
    }

    fun ZonedDateTime.toEndOfTheDay(): ZonedDateTime {
        return ZonedDateTime.of(this.toLocalDate(), LocalTime.of(23, 59), ZoneOffset.UTC)
    }

    fun ZonedDateTime.millis(): Long {
        return this.toInstant().toEpochMilli()
    }

}
