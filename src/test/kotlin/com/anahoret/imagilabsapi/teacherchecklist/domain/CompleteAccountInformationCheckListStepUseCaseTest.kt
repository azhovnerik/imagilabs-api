package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.common.testCompleteTeacherProfile
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.COMPLETE_YOUR_ACCOUNT_INFORMATION
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Complete account information check list step use case")
class CompleteAccountInformationCheckListStepUseCaseTest {

    private val completeTeacherCheckListStepUseCase = mockk<CompleteTeacherCheckListStepUseCase>()
    private val completeAccountInformationCheckListStepUseCase =
        CompleteAccountInformationCheckListStepUseCaseImpl(completeTeacherCheckListStepUseCase)

    private val teacherId = UUID.randomUUID()

    @Test
    fun `should complete step when all required fields are filled`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId)

        every {
            completeTeacherCheckListStepUseCase.complete(
                teacherId,
                COMPLETE_YOUR_ACCOUNT_INFORMATION
            )
        } returns mockk()

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 1) {
            completeTeacherCheckListStepUseCase.complete(
                teacherId,
                COMPLETE_YOUR_ACCOUNT_INFORMATION
            )
        }
    }

    @Test
    fun `should NOT complete step when subjects is null`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, subjects = null)

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when subjects is blank`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, subjects = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when schools is null`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, schools = null)

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when schools is blank`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, schools = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when schoolRoles is empty`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, schoolRoles = emptyList())

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when grades is empty`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, grades = emptyList())

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when firstName is blank`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, firstName = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when lastName is blank`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, lastName = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when email is blank`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, email = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when country is blank`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, country = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when organization is blank`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, organization = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should complete step when state is null (state is optional)`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, state = null)

        every {
            completeTeacherCheckListStepUseCase.complete(
                teacherId,
                COMPLETE_YOUR_ACCOUNT_INFORMATION
            )
        } returns mockk()

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 1) {
            completeTeacherCheckListStepUseCase.complete(
                teacherId,
                COMPLETE_YOUR_ACCOUNT_INFORMATION
            )
        }
    }

    @Test
    fun `should complete step when state is blank (state is optional)`() {
        val profile = testCompleteTeacherProfile(teacherId = teacherId, state = "  ")

        every {
            completeTeacherCheckListStepUseCase.complete(
                teacherId,
                COMPLETE_YOUR_ACCOUNT_INFORMATION
            )
        } returns mockk()

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 1) {
            completeTeacherCheckListStepUseCase.complete(
                teacherId,
                COMPLETE_YOUR_ACCOUNT_INFORMATION
            )
        }
    }
}
