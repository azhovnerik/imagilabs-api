package com.anahoret.imagilabsapi.teachers.domain

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Teacher get statistic use case")
class TeacherGetStatisticUseCaseTest {

    private val teacherStatisticService = mockk<TeacherStatisticService>()
    private val teacherProfileService = mockk<TeacherProfileService>()

    private val teacherGetStatisticUseCase = TeacherGetStatisticUseCaseImpl(
        teacherStatisticService, teacherProfileService
    )

    @Test
    fun `should return teacher not found error`() {
        val teacherId = UUID.randomUUID()

        every { teacherProfileService.exists(teacherId) } returns false

        val result = teacherGetStatisticUseCase.get(teacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return teacher statistic not found error`() {
        val teacherId = UUID.randomUUID()

        every { teacherProfileService.exists(teacherId) } returns true
        every { teacherStatisticService.getTeacherStatistic(teacherId) } returns null

        val result = teacherGetStatisticUseCase.get(teacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return teacher statistic`() {
        val teacherId = UUID.randomUUID()

        every { teacherProfileService.exists(teacherId) } returns true
        every { teacherStatisticService.getTeacherStatistic(teacherId) } returns mockk()

        val result = teacherGetStatisticUseCase.get(teacherId)

        assertTrue(result.isRight())
    }
}
