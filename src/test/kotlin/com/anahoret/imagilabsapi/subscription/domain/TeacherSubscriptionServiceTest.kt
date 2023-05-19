package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Clock
import java.time.Instant
import java.util.*

@DisplayName("Teacher subscription service")
class TeacherSubscriptionServiceTest {

    private val clock = mockk<Clock>()
    private val teacherProfileEntityRepository = mockk<TeacherProfileEntityRepository>()
    private val classroomService = mockk<ClassroomService>()
    private val teacherSubscriptionService = TeacherSubscriptionServiceImpl(
        teacherProfileEntityRepository,
        clock,
        classroomService
    )

    @DisplayName("when build subscription DTO")
    @Nested
    inner class BuildSubscriptionDtoTest {

        @Test
        fun `should return standard plan if start date is null`() {
            every { clock.instant() } returns Instant.ofEpochMilli(0)
            val result = teacherSubscriptionService.buildSubscriptionDto(null, 100)
            assertEquals(TeacherSubscriptionPlan.STANDARD, result.plan)
        }

        @Test
        fun `should return standard plan if end date is null`() {
            every { clock.instant() } returns Instant.ofEpochMilli(0)
            val result = teacherSubscriptionService.buildSubscriptionDto(100, null)
            assertEquals(TeacherSubscriptionPlan.STANDARD, result.plan)
        }

        @Test
        fun `should return standard plan if current time is before start time`() {
            every { clock.instant() } returns Instant.ofEpochMilli(0)
            val result = teacherSubscriptionService.buildSubscriptionDto(100, 200)
            assertEquals(TeacherSubscriptionPlan.STANDARD, result.plan)
        }

        @Test
        fun `should return standard plan if current time is after end time`() {
            every { clock.instant() } returns Instant.ofEpochMilli(300)
            val result = teacherSubscriptionService.buildSubscriptionDto(100, 200)
            assertEquals(TeacherSubscriptionPlan.STANDARD, result.plan)
        }

        @Test
        fun `should return pro plan if current time is between start and end`() {
            every { clock.instant() } returns Instant.ofEpochMilli(150)
            val result = teacherSubscriptionService.buildSubscriptionDto(100, 200)
            assertEquals(TeacherSubscriptionPlan.PRO, result.plan)
        }

    }

    @DisplayName("when checking the ability to create classrooms")
    @Nested
    inner class CheckAbilityToCreateClassroomTest {

        @DisplayName("when teacher has standard subscription")
        @Nested
        inner class StandardSubscriptionTest {

            private val teacherProfile = mockk<TeacherProfile> {
                every { id } returns UUID.randomUUID()
                every { subscription } returns mockk {
                    every { plan } returns TeacherSubscriptionPlan.STANDARD
                }
            }

            @ParameterizedTest
            @ValueSource(longs = [0L, 5L, 9L])
            fun `should return true if teacher has less than 10 classes`(classroomsCount: Long) {
                every { classroomService.countByTeacher(teacherProfile.id) } returns classroomsCount
                assertTrue(teacherSubscriptionService.canCreateClassroom(teacherProfile))
            }

            @ParameterizedTest
            @ValueSource(longs = [10L, 100L, Long.MAX_VALUE])
            fun `should return false if teacher has 10 classes or more`(classroomsCount: Long) {
                every { classroomService.countByTeacher(teacherProfile.id) } returns classroomsCount
                assertFalse(teacherSubscriptionService.canCreateClassroom(teacherProfile))
            }

        }

        @DisplayName("when teacher has pro subscription")
        @Nested
        inner class ProSubscriptionTest {

            private val teacherProfile = mockk<TeacherProfile> {
                every { id } returns UUID.randomUUID()
                every { subscription } returns mockk {
                    every { plan } returns TeacherSubscriptionPlan.PRO
                }
            }

            @ParameterizedTest
            @ValueSource(longs = [0L, 10L, 19L])
            fun `should return true if teacher has less than 20 classes`(classroomsCount: Long) {
                every { classroomService.countByTeacher(teacherProfile.id) } returns classroomsCount
                assertTrue(teacherSubscriptionService.canCreateClassroom(teacherProfile))
            }

            @ParameterizedTest
            @ValueSource(longs = [20L, 100L, Long.MAX_VALUE])
            fun `should return false if teacher has 20 classes or more`(classroomsCount: Long) {
                every { classroomService.countByTeacher(teacherProfile.id) } returns classroomsCount
                assertFalse(teacherSubscriptionService.canCreateClassroom(teacherProfile))
            }

        }

    }

}
