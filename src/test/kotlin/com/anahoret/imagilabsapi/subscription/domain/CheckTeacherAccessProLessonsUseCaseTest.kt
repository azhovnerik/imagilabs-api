package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Clock

@DisplayName("Check teacher access pro lessons use case")
class CheckTeacherAccessProLessonsUseCaseTest {

    private val clock = mockk<Clock>()
    private val checkTeacherAccessProLessonsUseCase = CheckTeacherAccessProLessonsUseCaseImpl(clock)

    @Test
    fun `should return teacher has no subscription error`() {
        val teacherProfile = mockk<TeacherProfile> {
            every { subscription.start } returns null
            every { subscription.end } returns null
        }

        val result = checkTeacherAccessProLessonsUseCase.checkAccess(teacherProfile)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return teacher subscription is canceled error`() {
        val teacherProfile = mockk<TeacherProfile> {
            every { subscription.start } returns 0
            every { subscription.end } returns 1
            every { subscription.canceled } returns true
        }

        val result = checkTeacherAccessProLessonsUseCase.checkAccess(teacherProfile)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return teacher must have subscription pro plan error`() {
        val teacherProfile = mockk<TeacherProfile> {
            every { subscription.plan } returns TeacherSubscriptionPlan.STANDARD
            every { subscription.start } returns 0
            every { subscription.end } returns 1
            every { subscription.canceled } returns false
        }

        every { clock.instant().toEpochMilli() } returns 2

        val result = checkTeacherAccessProLessonsUseCase.checkAccess(teacherProfile)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return teacher subscription expired error`() {
        val teacherProfile = mockk<TeacherProfile> {
            every { subscription.plan } returns TeacherSubscriptionPlan.PRO
            every { subscription.start } returns 0
            every { subscription.end } returns 1
            every { subscription.canceled } returns false
        }

        every { clock.instant().toEpochMilli() } returns 2

        val result = checkTeacherAccessProLessonsUseCase.checkAccess(teacherProfile)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return unit`() {
        val teacherProfile = mockk<TeacherProfile> {
            every { subscription.plan } returns TeacherSubscriptionPlan.PRO
            every { subscription.start } returns 0
            every { subscription.end } returns 2
            every { subscription.canceled } returns false
        }

        every { clock.instant().toEpochMilli() } returns 1

        val result = checkTeacherAccessProLessonsUseCase.checkAccess(teacherProfile)

        assertTrue(result.isRight())
    }
}
