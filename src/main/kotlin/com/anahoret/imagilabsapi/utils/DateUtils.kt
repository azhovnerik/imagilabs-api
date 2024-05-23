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

    fun LocalDate.toMidnight(): ZonedDateTime {
        return ZonedDateTime.of(this, LocalTime.MIDNIGHT, ZoneOffset.UTC)
    }

    fun LocalDate.toEndOfTheDay(): ZonedDateTime {
        return ZonedDateTime.of(this, LocalTime.of(23, 59, 59, 999), ZoneOffset.UTC)
    }

    fun ZonedDateTime.millis(): Long {
        return this.toInstant().toEpochMilli()
    }

    fun getCurrentTime(): LocalDateTime {
        return LocalDateTime.now(ZoneId.of(TimeZones.EUROPE_STOCKHOLM_VALUE))
    }
}
