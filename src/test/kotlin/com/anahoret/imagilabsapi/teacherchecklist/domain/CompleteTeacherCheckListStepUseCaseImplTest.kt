package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapSendAnalyticsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Complete teacher check list step use case")
class CompleteTeacherCheckListStepUseCaseImplTest {

    private val teacherCheckListService = mockk<TeacherCheckListService>()
    private val clevertapSendAnalyticsUseCase = mockk<ClevertapSendAnalyticsUseCase>()
    private val completeTeacherCheckListStepUseCase =
        CompleteTeacherCheckListStepUseCaseImpl(teacherCheckListService, clevertapSendAnalyticsUseCase)

    private val teacherId = UUID.randomUUID()
    private val step = TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM

    @Test
    fun `should complete step when it's not completed`() {
        every { teacherCheckListService.getCheckListStepByTeacherIdAndStep(teacherId, step) } returns mockk {
            every { completed } returns false
        }
        every { teacherCheckListService.completeCheckListStep(teacherId, step) } returns Unit
        every { clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(teacherId, step) } returns Unit
        every { teacherCheckListService.hasCompletedAllRequiredSteps(teacherId) } returns false
        every { teacherCheckListService.getCheckList(teacherId) } returns mockk()
        completeTeacherCheckListStepUseCase.complete(teacherId, step)
        verify(exactly = 1) { teacherCheckListService.completeCheckListStep(teacherId, step) }
    }

    @Test
    fun `should send complete onboarding step on Clevertap when it's not completed`() {
        every { teacherCheckListService.getCheckListStepByTeacherIdAndStep(teacherId, step) } returns mockk {
            every { completed } returns false
        }
        every { teacherCheckListService.completeCheckListStep(teacherId, step) } returns Unit
        every { clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(teacherId, step) } returns Unit
        every { teacherCheckListService.hasCompletedAllRequiredSteps(teacherId) } returns false
        every { teacherCheckListService.getCheckList(teacherId) } returns mockk()
        completeTeacherCheckListStepUseCase.complete(teacherId, step)
        verify(exactly = 1) { clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(teacherId, step) }
    }

    @Test
    fun `shouldn't complete step when it's completed`() {
        every { teacherCheckListService.getCheckListStepByTeacherIdAndStep(teacherId, step) } returns mockk {
            every { completed } returns true
        }
        every { teacherCheckListService.getCheckList(teacherId) } returns mockk()
        completeTeacherCheckListStepUseCase.complete(teacherId, step)
        verify(exactly = 0) { teacherCheckListService.completeCheckListStep(teacherId, step) }
    }

    @Test
    fun `shouldn't send complete onboarding step on Clevertap when it's completed`() {
        every { teacherCheckListService.getCheckListStepByTeacherIdAndStep(teacherId, step) } returns mockk {
            every { completed } returns true
        }
        every { teacherCheckListService.getCheckList(teacherId) } returns mockk()
        completeTeacherCheckListStepUseCase.complete(teacherId, step)
        verify(exactly = 0) { clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(teacherId, step) }
    }

    @Test
    fun `should return teacher check list`() {
        every { teacherCheckListService.getCheckListStepByTeacherIdAndStep(teacherId, step) } returns mockk {
            every { completed } returns true
        }
        every { teacherCheckListService.getCheckList(teacherId) } returns mockk {
            every { checkListSteps } returns listOf(mockk {
                every { completed } returns true
                every { step } returns TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM
            })
        }
        val result = completeTeacherCheckListStepUseCase.complete(teacherId, step)
        assertAll(
            { assertEquals(1, result.checkListSteps.size) },
            { assertEquals(TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM, result.checkListSteps[0].step) },
            { assertTrue(result.checkListSteps[0].completed) }
        )
    }

    @Test
    fun `should reset congratulation dialog when completing the last required step`() {
        every { teacherCheckListService.getCheckListStepByTeacherIdAndStep(teacherId, step) } returns mockk {
            every { completed } returns false
        }
        every { teacherCheckListService.completeCheckListStep(teacherId, step) } returns Unit
        every { clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(teacherId, step) } returns Unit
        every { teacherCheckListService.hasCompletedAllRequiredSteps(teacherId) } returns true
        every { teacherCheckListService.resetCongratulationDialog(teacherId) } returns Unit
        every { teacherCheckListService.getCheckList(teacherId) } returns mockk()

        completeTeacherCheckListStepUseCase.complete(teacherId, step)

        verify(exactly = 1) { teacherCheckListService.resetCongratulationDialog(teacherId) }
    }

    @Test
    fun `should NOT reset congratulation dialog when not all required steps are completed`() {
        every { teacherCheckListService.getCheckListStepByTeacherIdAndStep(teacherId, step) } returns mockk {
            every { completed } returns false
        }
        every { teacherCheckListService.completeCheckListStep(teacherId, step) } returns Unit
        every { clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(teacherId, step) } returns Unit
        every { teacherCheckListService.hasCompletedAllRequiredSteps(teacherId) } returns false
        every { teacherCheckListService.getCheckList(teacherId) } returns mockk()

        completeTeacherCheckListStepUseCase.complete(teacherId, step)

        verify(exactly = 0) { teacherCheckListService.resetCongratulationDialog(any()) }
    }

    @Test
    fun `should NOT reset congratulation dialog when step is already completed`() {
        every { teacherCheckListService.getCheckListStepByTeacherIdAndStep(teacherId, step) } returns mockk {
            every { completed } returns true
        }
        every { teacherCheckListService.getCheckList(teacherId) } returns mockk()

        completeTeacherCheckListStepUseCase.complete(teacherId, step)

        verify(exactly = 0) { teacherCheckListService.resetCongratulationDialog(any()) }
        verify(exactly = 0) { teacherCheckListService.hasCompletedAllRequiredSteps(any()) }
    }
}
