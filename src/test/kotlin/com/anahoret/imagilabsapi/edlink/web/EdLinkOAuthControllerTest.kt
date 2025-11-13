package com.anahoret.imagilabsapi.edlink.web

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.*
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenData
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomPermissions
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.common.web.ErrorResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.edlink.api.model.UnsupportedUserTypeError
import com.anahoret.imagilabsapi.edlink.domain.EdLinkOAuthCallbackRequest
import com.anahoret.imagilabsapi.edlink.domain.EdLinkOAuthStateService
import com.anahoret.imagilabsapi.edlink.domain.EdLinkOauthCallbackUseCase
import com.anahoret.imagilabsapi.users.UserType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.InternalAuthenticationServiceException
import java.util.*

@DisplayName("EdLink OAuth Controller")
class EdLinkOAuthControllerTest {

    private val edLinkOAuthStateService: EdLinkOAuthStateService = mockk()
    private val edLinkOauthCallbackUseCase: EdLinkOauthCallbackUseCase = mockk()
    private val requestAuthenticatorService: RequestAuthenticatorService = mockk()

    private val controller = EdLinkOAuthController(
        edLinkOAuthStateService,
        edLinkOauthCallbackUseCase,
        requestAuthenticatorService
    )

    @Test
    fun `createState should return success response with UUID`() {
        val expectedState = UUID.randomUUID()
        every { edLinkOAuthStateService.create() } returns expectedState

        val result = controller.createState()

        assertEquals(200, result.statusCode.value())
        val body = result.body
        assertNotNull(body)
        assertTrue(body is SuccessResponseDto<*>)
        assertEquals(expectedState.toString(), (body as SuccessResponseDto<String>).payload)

        verify(exactly = 1) { edLinkOAuthStateService.create() }
    }

    @Test
    fun `getToken should return 401 with AccessDeniedError message`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = false
        )

        every { edLinkOauthCallbackUseCase.tryAuthenticate(request, isLocalhostRedirect = false) } returns
                AccessDeniedError("INVALID_STATE").left()

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(401, result.statusCode.value())
        val body = result.body
        assertNotNull(body)
        assertTrue(body is ErrorResponseDto<*>)
        val errors = (body as ErrorResponseDto<*>).errors
        assertEquals(1, errors.size)
        assertEquals(401, errors[0].code)
        assertEquals("INVALID_STATE", errors[0].message)
    }

    @Test
    fun `getToken should return 401 with UnsupportedUserTypeError message`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = false
        )

        every { edLinkOauthCallbackUseCase.tryAuthenticate(request, isLocalhostRedirect = false) } returns
                UnsupportedUserTypeError.left()

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(401, result.statusCode.value())
        val body = result.body
        assertNotNull(body)
        assertTrue(body is ErrorResponseDto<*>)
        val errors = (body as ErrorResponseDto<*>).errors
        assertEquals(1, errors.size)
        assertEquals(401, errors[0].code)
        assertEquals("UNSUPPORTED_USER_TYPE", errors[0].message)
    }

    @Test
    fun `getToken should authenticate teacher successfully for web client`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val teacherProfile = testTeacher()
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = false
        )

        every {
            edLinkOauthCallbackUseCase.tryAuthenticate(
                request,
                isLocalhostRedirect = false
            )
        } returns teacherProfile.right()

        val authSuccess = TeacherAuthenticationSuccess(
            currentUser = TeacherUserData(
                id = teacherProfile.id,
                userType = UserType.TEACHER.name,
                profile = null
            ),
            jwtToken = JwtTokenData(token = "jwt-token", expiresAt = 123456789L)
        )
        every {
            requestAuthenticatorService.authenticateTeacher(
                teacherProfile.id,
                httpResponse,
                false
            )
        } returns authSuccess

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(200, result.statusCode.value())
        val body = result.body
        assertNotNull(body)
        assertTrue(body is SuccessResponseDto<*>)
        @Suppress("UNCHECKED_CAST") val payload = (body as SuccessResponseDto<TeacherAuthenticationSuccess>).payload
        assertEquals(authSuccess, payload)

        verify(exactly = 1) {
            requestAuthenticatorService.authenticateTeacher(
                teacherProfile.id,
                httpResponse,
                false
            )
        }
    }

    @Test
    fun `getToken should authenticate teacher successfully for mobile client`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val teacherProfile = testTeacher()
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = true
        )

        every {
            edLinkOauthCallbackUseCase.tryAuthenticate(
                request,
                isLocalhostRedirect = false
            )
        } returns teacherProfile.right()

        val authSuccess = TeacherAuthenticationSuccess(
            currentUser = TeacherUserData(
                id = teacherProfile.id,
                userType = UserType.TEACHER.name,
                profile = null
            ),
            jwtToken = JwtTokenData(token = "jwt-token", expiresAt = 123456789L)
        )
        every {
            requestAuthenticatorService.authenticateTeacher(
                teacherProfile.id,
                httpResponse,
                true
            )
        } returns authSuccess

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(200, result.statusCode.value())
        verify(exactly = 1) {
            requestAuthenticatorService.authenticateTeacher(
                teacherProfile.id,
                httpResponse,
                true
            )
        }
    }

    @Test
    fun `getToken should authenticate student successfully when classroom not blocked`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val classroomId = UUID.randomUUID()
        val studentProfile = testStudent()
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = false
        )

        val classroom = Classroom(
            id = classroomId,
            name = "Test classroom",
            accessCode = "ABCDEF",
            studentsCount = 10,
            projectsCount = 5,
            teacherId = UUID.randomUUID(),
            teachersCount = 1,
            blocked = false,
            ClassroomPermissions(canManageCoTeachers = true)
        )

        every {
            edLinkOauthCallbackUseCase.tryAuthenticate(
                request,
                isLocalhostRedirect = false
            )
        } returns studentProfile.right()

        val authSuccess = StudentAuthenticationSuccess(
            currentUser = StudentUserData(
                id = studentProfile.id,
                userType = UserType.STUDENT.name,
                profile = null
            ),
            currentClassroomId = classroomId,
            jwtToken = JwtTokenData(token = "jwt-token", expiresAt = 123456789L)
        )
        every {
            requestAuthenticatorService.authenticateStudent(
                studentProfile.id,
                httpResponse,
                false,
                classroomId
            )
        } returns authSuccess

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(200, result.statusCode.value())
        val body = result.body
        assertNotNull(body)
        assertTrue(body is SuccessResponseDto<*>)
        @Suppress("UNCHECKED_CAST") val payload = (body as SuccessResponseDto<StudentAuthenticationSuccess>).payload
        assertEquals(authSuccess, payload)

        verify(exactly = 1) {
            requestAuthenticatorService.authenticateStudent(
                studentProfile.id,
                httpResponse,
                false,
                classroomId
            )
        }
    }

    @Test
    fun `getToken should return 403 when student classroom is blocked`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val classroomId = UUID.randomUUID()
        val studentProfile = testStudent()
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = false
        )

        val classroom = Classroom(
            id = classroomId,
            name = "Blocked classroom",
            accessCode = "BLOCKED",
            studentsCount = 10,
            projectsCount = 5,
            teacherId = UUID.randomUUID(),
            teachersCount = 1,
            blocked = true,
            ClassroomPermissions(canManageCoTeachers = true)
        )

        every {
            edLinkOauthCallbackUseCase.tryAuthenticate(
                request,
                isLocalhostRedirect = false
            )
        } returns studentProfile.right()

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(403, result.statusCode.value())
        val body = result.body
        assertNotNull(body)
        assertTrue(body is ErrorResponseDto<*>)
        val errors = (body as ErrorResponseDto<*>).errors
        assertEquals(1, errors.size)
        assertEquals(403, errors[0].code)
        assertEquals("SUBSCRIPTION_REQUIRED", errors[0].message)

        verify(exactly = 0) { requestAuthenticatorService.authenticateStudent(any(), any(), any(), any()) }
    }

    @Test
    fun `getToken should throw exception when student classroom does not exist`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val classroomId = UUID.randomUUID()
        val studentProfile = testStudent()
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = false
        )

        every {
            edLinkOauthCallbackUseCase.tryAuthenticate(
                request,
                isLocalhostRedirect = false
            )
        } returns studentProfile.right()

        val ex = assertThrows(InternalAuthenticationServiceException::class.java) {
            controller.getToken(request, httpRequest, httpResponse)
        }

        assertEquals("CLASSROOM_DOES_NOT_EXIST", ex.message)
        verify(exactly = 0) { requestAuthenticatorService.authenticateStudent(any(), any(), any(), any()) }
    }

    @Test
    fun `getToken should authenticate student successfully for mobile client`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val classroomId = UUID.randomUUID()
        val studentProfile = testStudent()
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = true
        )

        val classroom = Classroom(
            id = classroomId,
            name = "Test classroom",
            accessCode = "MOBILE",
            studentsCount = 10,
            projectsCount = 5,
            teacherId = UUID.randomUUID(),
            teachersCount = 1,
            blocked = false,
            ClassroomPermissions(canManageCoTeachers = true)
        )

        every {
            edLinkOauthCallbackUseCase.tryAuthenticate(
                request,
                isLocalhostRedirect = false
            )
        } returns studentProfile.right()

        val authSuccess = StudentAuthenticationSuccess(
            currentUser = StudentUserData(
                id = studentProfile.id,
                userType = UserType.STUDENT.name,
                profile = null
            ),
            currentClassroomId = classroomId,
            jwtToken = JwtTokenData(token = "jwt-token", expiresAt = 123456789L)
        )
        every {
            requestAuthenticatorService.authenticateStudent(
                studentProfile.id,
                httpResponse,
                true,
                classroomId
            )
        } returns authSuccess

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(200, result.statusCode.value())
        verify(exactly = 1) {
            requestAuthenticatorService.authenticateStudent(
                studentProfile.id,
                httpResponse,
                true,
                classroomId
            )
        }
    }

    @Test
    fun `getToken should pass isLocalhostRedirect as true when Referer header is localhost 3000`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val teacherProfile = testTeacher()
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = false
        )

        every { httpRequest.getHeader(HttpHeaders.REFERER) } returns "http://localhost:3000/"
        every {
            edLinkOauthCallbackUseCase.tryAuthenticate(
                request,
                isLocalhostRedirect = true
            )
        } returns teacherProfile.right()

        val authSuccess = TeacherAuthenticationSuccess(
            currentUser = TeacherUserData(
                id = teacherProfile.id,
                userType = UserType.TEACHER.name,
                profile = null
            ),
            jwtToken = JwtTokenData(token = "jwt-token", expiresAt = 123456789L)
        )
        every {
            requestAuthenticatorService.authenticateTeacher(
                teacherProfile.id,
                httpResponse,
                false
            )
        } returns authSuccess

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(200, result.statusCode.value())
        verify(exactly = 1) { edLinkOauthCallbackUseCase.tryAuthenticate(request, isLocalhostRedirect = true) }
    }

    @Test
    fun `getToken should pass isLocalhostRedirect as false when Referer header is not localhost 3000`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val httpRequest = mockk<HttpServletRequest>(relaxed = true)
        val teacherProfile = testTeacher()
        val request = EdLinkOAuthCallbackRequest(
            state = UUID.randomUUID(),
            code = "test-code",
            mobileAppClient = false
        )

        every { httpRequest.getHeader(HttpHeaders.REFERER) } returns "https://app.imagilabs.com/"
        every {
            edLinkOauthCallbackUseCase.tryAuthenticate(
                request,
                isLocalhostRedirect = false
            )
        } returns teacherProfile.right()

        val authSuccess = TeacherAuthenticationSuccess(
            currentUser = TeacherUserData(
                id = teacherProfile.id,
                userType = UserType.TEACHER.name,
                profile = null
            ),
            jwtToken = JwtTokenData(token = "jwt-token", expiresAt = 123456789L)
        )
        every {
            requestAuthenticatorService.authenticateTeacher(
                teacherProfile.id,
                httpResponse,
                false
            )
        } returns authSuccess

        val result = controller.getToken(request, httpRequest, httpResponse)

        assertEquals(200, result.statusCode.value())
        verify(exactly = 1) { edLinkOauthCallbackUseCase.tryAuthenticate(request, isLocalhostRedirect = false) }
    }
}
