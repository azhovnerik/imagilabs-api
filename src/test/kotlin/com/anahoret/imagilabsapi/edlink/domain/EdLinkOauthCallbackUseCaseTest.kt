package com.anahoret.imagilabsapi.edlink.domain

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.edlink.api.EdLinkDistrictApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkIntegrationApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkProfileApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkTokenApi
import com.anahoret.imagilabsapi.edlink.api.model.*
import com.anahoret.imagilabsapi.signup.domain.TeacherSignUpUseCase
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("EdLink OAuth callback use case")
class EdLinkOauthCallbackUseCaseTest {

    private val edLinkOAuthStateService = mockk<EdLinkOAuthStateService>()
    private val edLinkProfileApi = mockk<EdLinkProfileApi>()
    private val edLinkTokenApi = mockk<EdLinkTokenApi>()
    private val edLinkIntegrationApi = mockk<EdLinkIntegrationApi>()
    private val studentProfileService = mockk<StudentProfileService>()
    private val teacherProfileService = mockk<TeacherProfileService>()
    private val teacherSignUpUseCase = mockk<TeacherSignUpUseCase>()
    private val edLinkDistrictApi = mockk<EdLinkDistrictApi>()

    private val useCase = EdLinkOauthCallbackUseCaseImpl(
        edLinkOAuthStateService,
        edLinkProfileApi,
        edLinkTokenApi,
        edLinkIntegrationApi,
        edLinkDistrictApi,
        studentProfileService,
        teacherProfileService,
        teacherSignUpUseCase
    )

    @Test
    fun `should return error when state is invalid`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)

        every { edLinkOAuthStateService.exists(state) } returns false

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isLeft())
        val error = result.leftOrNull() as AccessDeniedError
        assertEquals("INVALID_STATE", error.message)
        verify(exactly = 0) { edLinkOAuthStateService.delete(any()) }
    }

    @Test
    fun `should delete state after validation`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns mockk<OperationError>().left()

        useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        verify(exactly = 1) { edLinkOAuthStateService.delete(state) }
    }

    @Test
    fun `should return error when token exchange fails`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val error = mockk<OperationError>()

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns error.left()

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isLeft())
        assertEquals(error, result.leftOrNull())
    }

    @Test
    fun `should return error when profile fetch fails`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val error = mockk<OperationError>()

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns error.left()

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isLeft())
        assertEquals(error, result.leftOrNull())
    }

    @Test
    fun `should return error when integration fetch fails`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val person = createPerson(roles = listOf("teacher"))
        val error = mockk<OperationError>()

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns error.left()

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isLeft())
        assertEquals(error, result.leftOrNull())
    }

    @Test
    fun `should return error when user type is unsupported`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val person = createPerson(roles = listOf("admin"))
        val integration = MyIntegration(UUID.randomUUID())

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isLeft())
        assertEquals(UnsupportedUserTypeError, result.leftOrNull())
    }

    @Test
    fun `should return existing teacher profile when teacher already exists`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val integrationId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val person = createPerson(id = personId, roles = listOf("teacher"))
        val integration = MyIntegration(integrationId)
        val existingTeacher = mockk<TeacherProfile> {
            every { id } returns UUID.randomUUID()
        }

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()
        every { teacherProfileService.getByEdLink(integrationId, personId) } returns existingTeacher

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isRight())
        assertEquals(existingTeacher, result.getOrNull())
        verify(exactly = 0) { teacherSignUpUseCase.signUp(any()) }
    }

    @Test
    fun `should call exchange code to token method passing the isLocalhostRedirect flag`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val integrationId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val person = createPerson(id = personId, roles = listOf("teacher"))
        val integration = MyIntegration(integrationId)
        val existingTeacher = mockk<TeacherProfile> {
            every { id } returns UUID.randomUUID()
        }

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code", true) } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()
        every { teacherProfileService.getByEdLink(integrationId, personId) } returns existingTeacher

        useCase.tryAuthenticate(request, isLocalhostRedirect = true)

        verify(exactly = 1) { edLinkTokenApi.exchange("test-code", isLocalhostRedirect = true) }
    }

    @Test
    fun `should create new teacher profile when teacher does not exist`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val integrationId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val districtId = UUID.randomUUID()
        val person = createPerson(
            id = personId,
            email = "teacher@example.com",
            firstName = "John",
            lastName = "Doe",
            roles = listOf("teacher"),
            country = "US",
            districtId = districtId
        )
        val integration = MyIntegration(integrationId)
        val district = District(districtId, "District name")
        val newTeacher = mockk<TeacherProfile> {
            every { id } returns UUID.randomUUID()
        }

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()
        every { edLinkDistrictApi.myDistrict(token, districtId) } returns district.right()
        every { teacherProfileService.getByEdLink(integrationId, personId) } returns null
        every { teacherSignUpUseCase.signUp(any()) } returns newTeacher.right()

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isRight())
        assertEquals(newTeacher, result.getOrNull())
        verify(exactly = 1) {
            teacherSignUpUseCase.signUp(
                match {
                    it.email == "teacher@example.com" &&
                            it.password == "" &&
                            it.firstName == "John" &&
                            it.lastName == "Doe" &&
                            it.country == "US" &&
                            it.organization == "District name" &&
                            it.howDidYouHearAboutUs == "" &&
                            it.howDidYouHearAboutUsOther == null && !it.marketingEmailSubscribed && !it.mobileAppClient
                }
            )
        }
    }

    @Test
    fun `should set mobileAppClient flag when creating teacher for mobile client`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", true)
        val token = "access-token"
        val integrationId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val districtId = UUID.randomUUID()
        val person = createPerson(id = personId, roles = listOf("teacher"), districtId = districtId)
        val integration = MyIntegration(integrationId)
        val district = District(districtId, "District name")
        val newTeacher = mockk<TeacherProfile> {
            every { id } returns UUID.randomUUID()
        }

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()
        every { edLinkDistrictApi.myDistrict(token, districtId) } returns district.right()
        every { teacherProfileService.getByEdLink(integrationId, personId) } returns null
        every { teacherSignUpUseCase.signUp(any()) } returns newTeacher.right()

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isRight())
        verify(exactly = 1) {
            teacherSignUpUseCase.signUp(
                match { it.mobileAppClient }
            )
        }
    }

    @Test
    fun `should handle null country when creating teacher`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val integrationId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val districtId = UUID.randomUUID()
        val person = createPerson(id = personId, roles = listOf("teacher"), country = null, districtId = districtId)
        val integration = MyIntegration(integrationId)
        val district = District(districtId, "District name")
        val newTeacher = mockk<TeacherProfile> {
            every { id } returns UUID.randomUUID()
        }

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()
        every { edLinkDistrictApi.myDistrict(token, districtId) } returns district.right()
        every { teacherProfileService.getByEdLink(integrationId, personId) } returns null
        every { teacherSignUpUseCase.signUp(any()) } returns newTeacher.right()

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isRight())
        verify(exactly = 1) {
            teacherSignUpUseCase.signUp(
                match { it.country == "" }
            )
        }
    }

    @Test
    fun `should return error when teacher creation fails`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val integrationId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val districtId = UUID.randomUUID()
        val person = createPerson(id = personId, roles = listOf("teacher"), districtId = districtId)
        val integration = MyIntegration(integrationId)
        val validationErrors = listOf(mockk<ValidationError>())
        val district = District(districtId, "District name")

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()
        every { edLinkDistrictApi.myDistrict(token, districtId) } returns district.right()
        every { teacherProfileService.getByEdLink(integrationId, personId) } returns null
        every { teacherSignUpUseCase.signUp(any()) } returns validationErrors.left()

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isLeft())
        val error = result.leftOrNull() as AccessDeniedError
        assertEquals("TEACHER_CREATION_FAILED", error.message)
    }

    @Test
    fun `should return existing student profile when student exists`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val integrationId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val person = createPerson(id = personId, roles = listOf("student"))
        val integration = MyIntegration(integrationId)
        val existingStudent = mockk<StudentProfile> {
            every { id } returns UUID.randomUUID()
        }

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()
        every { studentProfileService.getByEdLink(integrationId, personId) } returns existingStudent

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isRight())
        assertEquals(existingStudent, result.getOrNull())
    }

    @Test
    fun `should return error when student does not exist`() {
        val state = UUID.randomUUID()
        val request = EdLinkOAuthCallbackRequest(state, "test-code", false)
        val token = "access-token"
        val integrationId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val person = createPerson(id = personId, roles = listOf("student"))
        val integration = MyIntegration(integrationId)

        every { edLinkOAuthStateService.exists(state) } returns true
        every { edLinkOAuthStateService.delete(state) } returns Unit
        every { edLinkTokenApi.exchange("test-code") } returns token.right()
        every { edLinkProfileApi.myProfile(token) } returns person.right()
        every { edLinkIntegrationApi.myIntegration(token) } returns integration.right()
        every { studentProfileService.getByEdLink(integrationId, personId) } returns null

        val result = useCase.tryAuthenticate(request, isLocalhostRedirect = false)

        assertTrue(result.isLeft())
        val error = result.leftOrNull() as AccessDeniedError
        assertEquals("STUDENT_NOT_FOUND", error.message)
    }

    private fun createPerson(
        id: UUID = UUID.randomUUID(),
        email: String = "test@example.com",
        firstName: String = "Test",
        lastName: String = "User",
        roles: List<String> = emptyList(),
        country: String? = "US",
        districtId: UUID = UUID.randomUUID(),
    ): Person {
        return Person(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            roles = roles,
            address = Address(country),
            districtId = districtId,
            displayName = "Test User"
        )
    }
}
