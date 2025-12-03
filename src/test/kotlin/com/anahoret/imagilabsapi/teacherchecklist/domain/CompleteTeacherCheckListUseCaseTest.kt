package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CONGRATULATION_DIALOG_SHOWN
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Complete teacher checklist use case")
class CompleteTeacherCheckListUseCaseTest {

    private val teacherCheckListService = mockk<TeacherCheckListService>()
    private val completeTeacherCheckListUseCase = CompleteTeacherCheckListUseCaseImpl(teacherCheckListService)

    @Test
    fun `should return validation error when teacher has not all completed steps`() {
        val testTeacher = testTeacher()

        every { teacherCheckListService.hasCompletedAllRequiredSteps(testTeacher.id) } returns false

        val result = completeTeacherCheckListUseCase.completeTeacherCheckList(testTeacher)

        verify { teacherCheckListService.hasCompletedAllRequiredSteps(testTeacher.id) }

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return success`() {
        val testTeacher = testTeacher()

        every { teacherCheckListService.hasCompletedAllRequiredSteps(testTeacher.id) } returns true
        every {
            teacherCheckListService.addTeacherCheckListStep(
                testTeacher.id,
                CONGRATULATION_DIALOG_SHOWN,
                true
            )
        } returns Unit

        val result = completeTeacherCheckListUseCase.completeTeacherCheckList(testTeacher)

        verify { teacherCheckListService.hasCompletedAllRequiredSteps(testTeacher.id) }

        assertTrue(result.isRight())
    }
}
