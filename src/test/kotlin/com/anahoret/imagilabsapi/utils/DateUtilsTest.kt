package com.anahoret.imagilabsapi.utils

import com.anahoret.imagilabsapi.utils.DateUtils.toEndOfTheDay
import com.anahoret.imagilabsapi.utils.DateUtils.toMidnight
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.DayOfWeek.THURSDAY
import java.time.Month.JANUARY

@DisplayName("Date utils test")
class DateUtilsTest {

    @Test
    fun `should convert to Stockholm format`() {
        val zonedDateTime = DateUtils.toStockholmDateTime(0)
        assertAll(
            { assertEquals(1970, zonedDateTime.year, "Incorrect year.") },
            { assertEquals(JANUARY, zonedDateTime.month, "Incorrect month.") },
            { assertEquals(1, zonedDateTime.dayOfMonth, "Incorrect day of month.") },
            { assertEquals(THURSDAY, zonedDateTime.dayOfWeek, "Incorrect day of week.") },
            { assertEquals(1, zonedDateTime.hour, "Incorrect hour.") },
            { assertEquals(0, zonedDateTime.minute, "Incorrect minute.") },
            { assertEquals(0, zonedDateTime.second, "Incorrect minute.") }
        )
    }

    @Test
    fun `should convert to UTC format`() {
        val zonedDateTime = DateUtils.toUTC(0)
        assertAll(
            { assertEquals(1970, zonedDateTime.year, "Incorrect year.") },
            { assertEquals(JANUARY, zonedDateTime.month, "Incorrect month.") },
            { assertEquals(1, zonedDateTime.dayOfMonth, "Incorrect day of month.") },
            { assertEquals(THURSDAY, zonedDateTime.dayOfWeek, "Incorrect day of week.") },
            { assertEquals(0, zonedDateTime.hour, "Incorrect hour.") },
            { assertEquals(0, zonedDateTime.minute, "Incorrect minute.") },
            { assertEquals(0, zonedDateTime.second, "Incorrect minute.") }
        )
    }

    @Test
    fun `should set zoned date time to midnight`() {
        val zonedDateTime = DateUtils.toUTC(0).toMidnight()
        assertAll(
            { assertEquals(1970, zonedDateTime.year, "Incorrect year.") },
            { assertEquals(JANUARY, zonedDateTime.month, "Incorrect month.") },
            { assertEquals(1, zonedDateTime.dayOfMonth, "Incorrect day of month.") },
            { assertEquals(THURSDAY, zonedDateTime.dayOfWeek, "Incorrect day of week.") },
            { assertEquals(0, zonedDateTime.hour, "Incorrect hour.") },
            { assertEquals(0, zonedDateTime.minute, "Incorrect minute.") },
            { assertEquals(0, zonedDateTime.second, "Incorrect minute.") }
        )
    }

    @Test
    fun `should set zoned date time to end of the day`() {
        val zonedDateTime = DateUtils.toUTC(0).toEndOfTheDay()
        assertAll(
            { assertEquals(1970, zonedDateTime.year, "Incorrect year.") },
            { assertEquals(JANUARY, zonedDateTime.month, "Incorrect month.") },
            { assertEquals(1, zonedDateTime.dayOfMonth, "Incorrect day of month.") },
            { assertEquals(THURSDAY, zonedDateTime.dayOfWeek, "Incorrect day of week.") },
            { assertEquals(23, zonedDateTime.hour, "Incorrect hour.") },
            { assertEquals(59, zonedDateTime.minute, "Incorrect minute.") },
            { assertEquals(0, zonedDateTime.second, "Incorrect minute.") }
        )
    }
}
