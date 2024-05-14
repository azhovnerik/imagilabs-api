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

    private val teacherProfileEntity = mockk<TeacherSubscriptionData>()

    @DisplayName("when build subscription DTO")
    @Nested
    inner class BuildSubscriptionDtoTest {

        @Test
        fun `should return standard plan if start date is null`() {
            every { clock.instant() } returns Instant.ofEpochMilli(0)
            every { teacherProfileEntity.subscriptionStart } returns null
            every { teacherProfileEntity.subscriptionEnd } returns 100
            every { teacherProfileEntity.subscriptionCanceled } returns false

            val result = teacherSubscriptionService.buildSubscriptionDto(teacherProfileEntity)
            assertEquals(TeacherSubscriptionPlan.STANDARD, result.plan)
        }

        @Test
        fun `should return standard plan if end date is null`() {
            every { clock.instant() } returns Instant.ofEpochMilli(0)
            every { teacherProfileEntity.subscriptionStart } returns 100
            every { teacherProfileEntity.subscriptionEnd } returns null
            every { teacherProfileEntity.subscriptionCanceled } returns false

            val result = teacherSubscriptionService.buildSubscriptionDto(teacherProfileEntity)
            assertEquals(TeacherSubscriptionPlan.STANDARD, result.plan)
        }

        @Test
        fun `should return standard plan if current time is before start time`() {
            every { clock.instant() } returns Instant.ofEpochMilli(0)
            every { teacherProfileEntity.subscriptionStart } returns 100
            every { teacherProfileEntity.subscriptionEnd } returns 200
            every { teacherProfileEntity.subscriptionCanceled } returns false

            val result = teacherSubscriptionService.buildSubscriptionDto(teacherProfileEntity)
            assertEquals(TeacherSubscriptionPlan.STANDARD, result.plan)
        }

        @Test
        fun `should return standard plan if current time is after end time`() {
            every { clock.instant() } returns Instant.ofEpochMilli(300)
            every { teacherProfileEntity.subscriptionStart } returns 100
            every { teacherProfileEntity.subscriptionEnd } returns 200
            every { teacherProfileEntity.subscriptionCanceled } returns false

            val result = teacherSubscriptionService.buildSubscriptionDto(teacherProfileEntity)
            assertEquals(TeacherSubscriptionPlan.STANDARD, result.plan)
        }

        @Test
        fun `should return pro plan if current time is between start and end`() {
            every { clock.instant() } returns Instant.ofEpochMilli(150)
            every { teacherProfileEntity.subscriptionStart } returns 100
            every { teacherProfileEntity.subscriptionEnd } returns 200
            every { teacherProfileEntity.subscriptionCanceled } returns false

            val result = teacherSubscriptionService.buildSubscriptionDto(teacherProfileEntity)
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

            @Test
            fun `should return true if teacher has less than 1 classes`() {
                every { classroomService.countByTeacher(teacherProfile.id) } returns 0L
                assertTrue(teacherSubscriptionService.canCreateClassroom(teacherProfile))
            }

            @ParameterizedTest
            @ValueSource(longs = [1L, 10L, 100L, Long.MAX_VALUE])
            fun `should return false if teacher has 1 class or more`(classroomsCount: Long) {
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
            @ValueSource(longs = [0L, 10L, 19L, 29L])
            fun `should return true if teacher has less classes than allowed`(classroomsCount: Long) {
                every { classroomService.countByTeacher(teacherProfile.id) } returns classroomsCount
                assertTrue(teacherSubscriptionService.canCreateClassroom(teacherProfile))
            }

            @ParameterizedTest
            @ValueSource(longs = [30L, 100L, Long.MAX_VALUE])
            fun `should return false if teacher has not allowed amount of classes`(classroomsCount: Long) {
                every { classroomService.countByTeacher(teacherProfile.id) } returns classroomsCount
                assertFalse(teacherSubscriptionService.canCreateClassroom(teacherProfile))
            }

        }

    }

    @DisplayName("when checking the ability to add students to classroom")
    @Nested
    inner class CheckAbilityToAddStudentsToClassroomTest {

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
            @ValueSource(longs = [0L, 25L, 50L])
            fun `should return false if teacher has no more than 50 students in the classroom`(studentCountInClassroom: Long) {
                assertFalse(teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, studentCountInClassroom))
            }

            @ParameterizedTest
            @ValueSource(longs = [51L, 100L, Long.MAX_VALUE])
            fun `should return true if teacher has more than 50 students in the classroom`(studentCountInClassroom: Long) {
                assertTrue(teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, studentCountInClassroom))
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
            @ValueSource(longs = [0L, 100L, 200L])
            fun `should return false if teacher has no more than 200 students in the classroom`(studentCountInClassroom: Long) {
                assertFalse(teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, studentCountInClassroom))
            }

            @ParameterizedTest
            @ValueSource(longs = [201L, 500L, Long.MAX_VALUE])
            fun `should return true if teacher has more than 200 students in the classroom`(studentCountInClassroom: Long) {
                assertTrue(teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, studentCountInClassroom))
            }

        }

    }

}
