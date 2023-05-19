package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant

@DisplayName("Teacher subscription service")
class TeacherSubscriptionServiceTest {

    private val clock = mockk<Clock>()
    private val teacherProfileEntityRepository = mockk<TeacherProfileEntityRepository>()
    private val teacherSubscriptionService = TeacherSubscriptionServiceImpl(
        teacherProfileEntityRepository,
        clock
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

}
