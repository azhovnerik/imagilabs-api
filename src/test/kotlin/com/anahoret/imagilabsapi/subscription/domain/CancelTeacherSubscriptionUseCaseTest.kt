package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.common.testAdmin
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Cancel teacher subscription use case test")
class CancelTeacherSubscriptionUseCaseTest {

    private val teacherSubscriptionService =  mockk<TeacherSubscriptionService>()
    private val teacherProfileService = mockk<TeacherProfileService>()
    private val exportUseCase = mockk<GoogleSheetsTeachersExportUseCase>()

    private val cancelTeacherSubscriptionUseCase = CancelTeacherSubscriptionUseCaseImpl(
        teacherSubscriptionService, teacherProfileService, exportUseCase
    )

    @DisplayName("cancel teacher subscription by admin")
    @Nested
    inner class CancelTeacherSubscriptionByAdmin {

        @Test
        fun `should return teacher not found return`() {
            val teacherId = UUID.randomUUID()
            val adminProfile = testAdmin()

            every { teacherProfileService.exists(teacherId) } returns false

            val result = cancelTeacherSubscriptionUseCase.cancel(teacherId, adminProfile)

            assertTrue(result.isLeft())
        }

        @Test
        fun `should return unit`() {
            val teacherId = UUID.randomUUID()
            val adminProfile = testAdmin()

            every { teacherProfileService.exists(teacherId) } returns true
            every { exportUseCase.updateAsync(teacherId) } returns Unit
            every { teacherSubscriptionService.cancelSubscription(teacherId) } returns Unit

            val result = cancelTeacherSubscriptionUseCase.cancel(teacherId, adminProfile)

            assertTrue(result.isRight())
        }
    }

    @DisplayName("cancel teacher subscription by teacher")
    @Nested
    inner class CancelTeacherSubscriptionByTeacher {

        @Test
        fun `should return teacher not found return`() {
            val teacherId = UUID.randomUUID()
            val teacherProfile = testTeacher()

            every { teacherProfileService.exists(teacherId) } returns false

            val result = cancelTeacherSubscriptionUseCase.cancel(teacherId, teacherProfile)

            assertTrue(result.isLeft())
        }

        @Test
        fun `should return teacher can cancel only his subscription error`() {
            val teacherId = UUID.randomUUID()
            val teacherProfile = testTeacher()

            every { teacherProfileService.exists(teacherId) } returns true

            val result = cancelTeacherSubscriptionUseCase.cancel(teacherId, teacherProfile)

            println(result.isLeft())

            assertTrue(result.isLeft())
        }

        @Test
        fun `should return unit`() {
            val teacherProfile = testTeacher()

            println(teacherProfile.id)
            every { teacherProfileService.exists(teacherProfile.id) } returns true
            every { exportUseCase.updateAsync(teacherProfile.id) } returns Unit
            every { teacherSubscriptionService.cancelSubscription(teacherProfile.id) } returns Unit

            val result = cancelTeacherSubscriptionUseCase.cancel(teacherProfile.id, teacherProfile)

            assertTrue(result.isRight())
        }
    }
}
