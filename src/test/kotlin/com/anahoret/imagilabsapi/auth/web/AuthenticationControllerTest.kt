package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.auth.domain.ImagiLabsAuthenticationToken
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenData
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomPermissions
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.web.ErrorResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.students.domain.StudentLoginRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherResetPasswordUseCase
import com.anahoret.imagilabsapi.users.UserType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InternalAuthenticationServiceException
import java.util.*

class AuthenticationControllerTest {

    private val authenticationManager: AuthenticationManager = mockk()
    private val requestAuthenticatorService: RequestAuthenticatorService = mockk()
    private val classroomService: ClassroomService = mockk()
    private val teacherResetPasswordUseCase: TeacherResetPasswordUseCase = mockk()

    private val controller = AuthenticationController(
        authenticationManager,
        requestAuthenticatorService,
        classroomService,
        teacherResetPasswordUseCase
    )

    @Test
    fun `studentLogin returns 403 when classroom is blocked`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val accessCode = "ABCDEF"
        val classroom = Classroom(
            id = UUID.randomUUID(),
            name = "Blocked class",
            accessCode = accessCode,
            studentsCount = 0,
            projectsCount = 0,
            teacherId = UUID.randomUUID(),
            teachersCount = 1,
            blocked = true,
            ClassroomPermissions(true)
        )
        every { classroomService.getByAccessCode(accessCode) } returns classroom

        val request = StudentLoginRequest(
            username = "student1",
            classroomAccessCode = accessCode,
            password = "pwd",
            mobileAppClient = false
        )

        val result = controller.studentLogin(request, httpResponse)

        assertEquals(403, result.statusCode.value())
        val body = result.body
        assertNotNull(body)
        assertTrue(body is ErrorResponseDto<*>)
        val errors = (body as ErrorResponseDto<*>).errors
        assertEquals(1, errors.size)
        assertEquals(403, errors[0].code)
        assertEquals("SUBSCRIPTION_REQUIRED", errors[0].message)

        verify(exactly = 0) { authenticationManager.authenticate(any()) }
    }

    @Test
    fun `studentLogin authenticates and returns 200 when classroom not blocked`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val accessCode = "GHIJKL"
        val classroom = Classroom(
            id = UUID.randomUUID(),
            name = "Open class",
            accessCode = accessCode,
            studentsCount = 0,
            projectsCount = 0,
            teacherId = UUID.randomUUID(),
            teachersCount = 1,
            blocked = false,
            ClassroomPermissions(true)
        )
        every { classroomService.getByAccessCode(accessCode) } returns classroom

        val request = StudentLoginRequest(
            username = "student2",
            classroomAccessCode = accessCode,
            password = "pwd2",
            mobileAppClient = false
        )

        val studentProfile = testStudent()
        val returnedToken = ImagiLabsAuthenticationToken(studentProfile, UserType.STUDENT, request)
        every { authenticationManager.authenticate(any()) } returns returnedToken

        every {
            requestAuthenticatorService.authenticateStudent(
                studentProfile.id,
                any(),
                request.mobileAppClient,
                classroom.id
            )
        } returns StudentAuthenticationSuccess(
            currentUser = StudentUserData(
                id = studentProfile.id,
                userType = UserType.STUDENT.name,
                profile = null,
            ),
            currentClassroomId = classroom.id,
            jwtToken = JwtTokenData(token = "token", expiresAt = 0)
        )

        val result = controller.studentLogin(request, httpResponse)

        assertEquals(200, result.statusCode.value())
        assertTrue(result.body is SuccessResponseDto<*>)
        val payload = (result.body as SuccessResponseDto<StudentAuthenticationSuccess?>).payload
        assertNotNull(payload)
        assertEquals(studentProfile.id, payload!!.currentUser.id)

        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        verify(exactly = 1) {
            requestAuthenticatorService.authenticateStudent(
                studentProfile.id,
                any(),
                request.mobileAppClient,
                classroom.id
            )
        }
    }

    @Test
    fun `studentLogin throws when classroom does not exist`() {
        val httpResponse = mockk<HttpServletResponse>(relaxed = true)
        val accessCode = "NOCLASS"

        every { classroomService.getByAccessCode(accessCode) } returns null

        val request = StudentLoginRequest(
            username = "student3",
            classroomAccessCode = accessCode,
            password = "pwd3",
            mobileAppClient = false
        )

        val ex = assertThrows(InternalAuthenticationServiceException::class.java) {
            controller.studentLogin(request, httpResponse)
        }
        assertEquals("CLASSROOM_DOES_NOT_EXIST", ex.message)

        verify(exactly = 0) { authenticationManager.authenticate(any()) }
    }
}
