package com.anahoret.imagilabsapi.debuggingbuddy.service

import com.anahoret.imagilabsapi.openai.domain.EnvironmentPermissionServiceProduction
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.*
import java.util.*

@DisplayName("Environment permission service production")
class EnvironmentPermissionServiceProductionTest {

    private val clock = mockk<Clock>()
    private val teacherProfileService = mockk<TeacherProfileService>()
    private val environmentPermissionServiceProduction = EnvironmentPermissionServiceProduction(
        teacherProfileService,
        clock
    )
    private val studentId = UUID.randomUUID()

    @Test
    fun `should return true when user is teacher and date is before October 18 2024`() {
        val september162024 = ZonedDateTime.of(2024, 9, 16, 0, 0, 0, 0, ZoneId.of("UTC-7"))
        every { clock.millis() } returns september162024.toInstant().toEpochMilli()
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(mockk<TeacherProfile>()))
    }

    @Test
    fun `should return true when user is teacher and date is after October 18 2024`() {
        val october192024 = ZonedDateTime.of(LocalDate.of(2024, 10, 19), LocalTime.MIN, ZoneId.of("UTC-7"))
        every { clock.millis() } returns october192024.toInstant().toEpochMilli()
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(mockk<TeacherProfile>()))
    }

    @Test
    fun `should return true when user is student and their teacher has subscription  and date is after October 18 2024`() {
        val october192024 =
            ZonedDateTime.of(LocalDate.of(2024, 10, 19), LocalTime.MIN, ZoneId.of("UTC-7")).toInstant().toEpochMilli()
        every { clock.millis() } returns october192024

        every { teacherProfileService.getTeacherByStudent(studentId) } returns mockk<TeacherProfile> {
            every { hasProSubscription(october192024) } returns true
        }
        val studentProfile = mockk<StudentProfile> {
            every { id } returns studentId
        }
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(studentProfile))
    }

    @Test
    fun `should return true when date is between November 8 and December 15 of 2025`() {
        val november82025 = ZonedDateTime.of(2025, 11, 8, 0, 0, 0, 0, ZoneId.of("UTC-8"))
        every { clock.millis() } returns november82025.toInstant().toEpochMilli()
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(mockk<StudentProfile>()))
    }

    @Test
    fun `should return false when user is student and their teacher has no subscription and date is after December 15 of 2025`() {
        val december162025 =
            ZonedDateTime.of(LocalDate.of(2025, 12, 16), LocalTime.MIN, ZoneId.of("UTC-8")).toInstant().toEpochMilli()
        every { clock.millis() } returns december162025
        every { teacherProfileService.getTeacherByStudent(studentId) } returns mockk<TeacherProfile> {
            every { hasProSubscription(december162025) } returns false
        }
        val studentProfile = mockk<StudentProfile> {
            every { id } returns studentId
        }
        assertFalse(environmentPermissionServiceProduction.canGetAssistanceForProject(studentProfile))
    }

    @Test
    fun `should return false when user is student and their teacher has no subscription and date is before November 8 of 2025`() {
        val november72025 =
            ZonedDateTime.of(LocalDate.of(2025, 11, 7), LocalTime.MAX, ZoneId.of("UTC-8")).toInstant().toEpochMilli()
        every { clock.millis() } returns november72025
        every { teacherProfileService.getTeacherByStudent(studentId) } returns mockk<TeacherProfile> {
            every { hasProSubscription(november72025) } returns false
        }
        val studentProfile = mockk<StudentProfile> {
            every { id } returns studentId
        }
        assertFalse(environmentPermissionServiceProduction.canGetAssistanceForProject(studentProfile))
    }

}
