package com.anahoret.imagilabsapi.subscription.domain

import arrow.core.getOrElse
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Set subscription period use case")
class SetSubscriptionPeriodUseCaseTest {

    private val teacherSubscriptionService = mockk<TeacherSubscriptionService>()
    private val teacherProfileService = mockk<TeacherProfileService>()
    private val googleSheetsTeachersExportUseCase = mockk<GoogleSheetsTeachersExportUseCase>()
    private val setSubscriptionPeriodUseCaseImpl = SetSubscriptionPeriodUseCaseImpl(
        teacherSubscriptionService,
        teacherProfileService,
        googleSheetsTeachersExportUseCase
    )

    @Test
    fun `should return error if end date is before start date`() {
        val teacherId = UUID.randomUUID()
        val request = SetSubscriptionPeriodRequest(200, 100)
        val result = setSubscriptionPeriodUseCaseImpl.set(teacherId, request)
        assertTrue(result.getOrElse { it } is SubscriptionPeriodInvalid)
    }

    @Test
    fun `should call teacher subscription service to update subscription period`() {
        val teacherId = UUID.randomUUID()
        val request = SetSubscriptionPeriodRequest(100, 200)
        every { teacherProfileService.exists(teacherId) } returns true
        every { googleSheetsTeachersExportUseCase.updateAsync(teacherId) } returns Unit
        justRun { teacherSubscriptionService.setPeriod(teacherId, 100, 200) }
        setSubscriptionPeriodUseCaseImpl.set(teacherId, request)
        verify { teacherSubscriptionService.setPeriod(teacherId, 100, 200) }
    }

    @Test
    fun `should return error if teacher does not exist`() {
        val teacherId = UUID.randomUUID()
        val request = SetSubscriptionPeriodRequest(100, 200)
        every { teacherProfileService.exists(teacherId) } returns false
        val result = setSubscriptionPeriodUseCaseImpl.set(teacherId, request)
        assertTrue(result.getOrElse { it } is NotFoundError)
    }

}
