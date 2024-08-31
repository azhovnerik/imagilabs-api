package com.anahoret.imagilabsapi.debuggingbuddy.service

import com.anahoret.imagilabsapi.openai.domain.EnvironmentPermissionServiceProduction
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

@DisplayName("Environment permission service production")
class EnvironmentPermissionServiceProductionTest {

    private val clock = mockk<Clock>()
    private val environmentPermissionServiceProduction = EnvironmentPermissionServiceProduction(clock)

    @Test
    fun `should return true when teacher email is in Anadea domain`() {
        every { clock.millis() } returns 0
        val teacherProfile = mockk<TeacherProfile> {
            every { email } returns "teacher@anadeainc.com"
        }
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(teacherProfile))
    }

    @Test
    fun `should return true when teacher email is in ImagiLabs domain`() {
        every { clock.millis() } returns 0
        val teacherProfile = mockk<TeacherProfile> {
            every { email } returns "teacher@imagilabs.com"
        }
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(teacherProfile))
    }

    @Test
    fun `should return true if the date is September 16 2024`() {
        val september162024 = ZonedDateTime.of(2024, 9, 16, 0, 0, 0, 0, ZoneId.of("UTC-7"))
        every { clock.millis() } returns september162024.toInstant().toEpochMilli()
        val teacherProfile = mockk<TeacherProfile>()
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(teacherProfile))
    }

    @Test
    fun `should return false if teacher email is in other domain and the date is before September 16 2024`() {
        val september162024 = ZonedDateTime.of(2024, 9, 16, 0, 0, 0, 0, ZoneId.of("UTC-7"))
        every { clock.millis() } returns september162024.toInstant().toEpochMilli() - 1
        val teacherProfile = mockk<TeacherProfile> {
            every { email } returns "teacher@example.com"
        }
        assertFalse(environmentPermissionServiceProduction.canGetAssistanceForProject(teacherProfile))
    }

}
