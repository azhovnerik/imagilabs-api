package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.STANDARD
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.COMPLETE_YOUR_ACCOUNT_INFORMATION
import com.anahoret.imagilabsapi.teachers.domain.GradeLevel
import com.anahoret.imagilabsapi.teachers.domain.SchoolRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
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
        val profile = createCompleteTeacherProfile()

        every { completeTeacherCheckListStepUseCase.complete(teacherId, COMPLETE_YOUR_ACCOUNT_INFORMATION) } returns mockk()

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 1) { completeTeacherCheckListStepUseCase.complete(teacherId, COMPLETE_YOUR_ACCOUNT_INFORMATION) }
    }

    @Test
    fun `should NOT complete step when subjects is null`() {
        val profile = createTeacherProfile(subjects = null)

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when subjects is blank`() {
        val profile = createTeacherProfile(subjects = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when schools is null`() {
        val profile = createTeacherProfile(schools = null)

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when schools is blank`() {
        val profile = createTeacherProfile(schools = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when schoolRoles is empty`() {
        val profile = createTeacherProfile(schoolRoles = emptyList())

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when grades is empty`() {
        val profile = createTeacherProfile(grades = emptyList())

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when firstName is blank`() {
        val profile = createTeacherProfile(firstName = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when lastName is blank`() {
        val profile = createTeacherProfile(lastName = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when email is blank`() {
        val profile = createTeacherProfile(email = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when country is blank`() {
        val profile = createTeacherProfile(country = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should NOT complete step when organization is blank`() {
        val profile = createTeacherProfile(organization = "  ")

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 0) { completeTeacherCheckListStepUseCase.complete(any(), any()) }
    }

    @Test
    fun `should complete step when state is null (state is optional)`() {
        val profile = createTeacherProfile(state = null)

        every { completeTeacherCheckListStepUseCase.complete(teacherId, COMPLETE_YOUR_ACCOUNT_INFORMATION) } returns mockk()

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 1) { completeTeacherCheckListStepUseCase.complete(teacherId, COMPLETE_YOUR_ACCOUNT_INFORMATION) }
    }

    @Test
    fun `should complete step when state is blank (state is optional)`() {
        val profile = createTeacherProfile(state = "  ")

        every { completeTeacherCheckListStepUseCase.complete(teacherId, COMPLETE_YOUR_ACCOUNT_INFORMATION) } returns mockk()

        completeAccountInformationCheckListStepUseCase.checkAndComplete(profile)

        verify(exactly = 1) { completeTeacherCheckListStepUseCase.complete(teacherId, COMPLETE_YOUR_ACCOUNT_INFORMATION) }
    }

    private fun createCompleteTeacherProfile(): TeacherProfile {
        return createTeacherProfile()
    }

    private fun createTeacherProfile(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        country: String = "USA",
        organization: String = "Test School",
        state: String? = "California",
        schoolRoles: List<SchoolRole> = listOf(SchoolRole.TEACHER),
        grades: List<GradeLevel> = listOf(GradeLevel.NINTH_GRADE),
        subjects: String? = "Math, Science",
        schools: String? = "Test High School"
    ): TeacherProfile {
        return TeacherProfile(
            id = teacherId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            country = country,
            organization = organization,
            createdAt = 0L,
            emailVerified = true,
            marketingEmailSubscribed = false,
            subscription = TeacherSubscription(null, null, STANDARD, false, teacherId),
            state = state,
            schoolRoles = schoolRoles,
            grades = grades,
            subjects = subjects,
            schools = schools
        )
    }
}