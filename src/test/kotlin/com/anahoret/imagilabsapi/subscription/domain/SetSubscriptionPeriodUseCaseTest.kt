package com.anahoret.imagilabsapi.subscription.domain

import arrow.core.getOrElse
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
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

    private val start = LocalDate.of(1970, 1, 1)
    private val end = LocalDate.of(1970, 1, 2)

    @Test
    fun `should return error if end date is before start date`() {
        val teacherId = UUID.randomUUID()
        val request = SetSubscriptionPeriodRequest(end, start)
        val result = setSubscriptionPeriodUseCaseImpl.set(teacherId, request)
        assertTrue(result.getOrElse { it } is SubscriptionPeriodInvalid)
    }

    @Test
    fun `should call teacher subscription service to update subscription period`() {
        val teacherId = UUID.randomUUID()
        val request = SetSubscriptionPeriodRequest(start, end)
        every { teacherProfileService.exists(teacherId) } returns true
        justRun { googleSheetsTeachersExportUseCase.updateAsync(teacherId) }
        justRun { teacherSubscriptionService.setPeriod(teacherId, 0, 172799000) }

        setSubscriptionPeriodUseCaseImpl.set(teacherId, request)

        verify { teacherSubscriptionService.setPeriod(teacherId, 0, 172799000) }
    }

    @Test
    fun `should return error if teacher does not exist`() {
        val teacherId = UUID.randomUUID()
        val request = SetSubscriptionPeriodRequest(start, end)
        every { teacherProfileService.exists(teacherId) } returns false
        val result = setSubscriptionPeriodUseCaseImpl.set(teacherId, request)
        assertTrue(result.getOrElse { it } is NotFoundError)
    }

}
