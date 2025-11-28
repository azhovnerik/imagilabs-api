package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.teacherchecklist.domain.CompleteAccountInformationCheckListStepUseCase
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Teacher profile update use case")
class TeacherProfileUpdateUseCaseTest {

    private val teacherProfileService = mockk<TeacherProfileService>()
    private val completeAccountInformationCheckListStepUseCase = mockk<CompleteAccountInformationCheckListStepUseCase>()

    private val useCase = TeacherProfileUpdateUseCaseImpl(
        teacherProfileService,
        completeAccountInformationCheckListStepUseCase
    )

    @Test
    fun `should return teacher not found error when teacher does not exist`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(firstName = "John")

        every { teacherProfileService.updateProfile(teacherId, request) } returns null

        val result = useCase.update(teacherId, request)

        assertTrue(result.isLeft())
        verify { teacherProfileService.updateProfile(teacherId, request) }
    }

    @Test
    fun `should return updated teacher profile on success`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(
            firstName = "John",
            lastName = "Doe"
        )
        val updatedProfile = mockk<TeacherProfile>()

        every { teacherProfileService.updateProfile(teacherId, request) } returns updatedProfile
        justRun { completeAccountInformationCheckListStepUseCase.checkAndComplete(updatedProfile) }

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        result.map { profile ->
            assertEquals(updatedProfile, profile)
        }
        verify { teacherProfileService.updateProfile(teacherId, request) }
    }

    @Test
    fun `should delegate update to service`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(
            firstName = "Jane",
            schoolRoles = listOf(SchoolRole.TEACHER),
            grades = listOf(GradeLevel.FIRST_GRADE)
        )
        val updatedProfile = mockk<TeacherProfile>()

        every { teacherProfileService.updateProfile(teacherId, request) } returns updatedProfile
        justRun { completeAccountInformationCheckListStepUseCase.checkAndComplete(updatedProfile) }

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        verify(exactly = 1) { teacherProfileService.updateProfile(teacherId, request) }
    }

    @Test
    fun `should check and complete account information step after successful update`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(
            firstName = "Jane",
            lastName = "Doe",
            subjects = "Math"
        )
        val updatedProfile = mockk<TeacherProfile>()

        every { teacherProfileService.updateProfile(teacherId, request) } returns updatedProfile
        justRun { completeAccountInformationCheckListStepUseCase.checkAndComplete(updatedProfile) }

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        verify(exactly = 1) { completeAccountInformationCheckListStepUseCase.checkAndComplete(updatedProfile) }
    }

    @Test
    fun `should NOT check account information step when teacher not found`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(firstName = "John")

        every { teacherProfileService.updateProfile(teacherId, request) } returns null

        val result = useCase.update(teacherId, request)

        assertTrue(result.isLeft())
        verify(exactly = 0) { completeAccountInformationCheckListStepUseCase.checkAndComplete(any()) }
    }
}
