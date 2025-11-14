package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.admins.domain.AdminProfileService
import com.anahoret.imagilabsapi.auth.web.jwt.JwtProperties
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenData
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.mockk.*
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import java.time.Duration
import java.time.Instant
import java.util.*

class RequestAuthenticatorServiceImplTest {

    private val jwtProperties = JwtProperties(
        secret = "secret",
        ttlWeb = Duration.ofHours(1),
        ttlMobile = Duration.ofHours(2)
    )

    private val jwtTokenUtil: JwtTokenUtil = mockk()
    private val teacherProfileService: TeacherProfileService = mockk(relaxed = true)
    private val studentProfileService: StudentProfileService = mockk(relaxed = true)
    private val adminProfileService: AdminProfileService = mockk(relaxed = true)

    private val service = RequestAuthenticatorServiceImpl(
        jwtTokenUtil,
        jwtProperties,
        teacherProfileService,
        studentProfileService,
        adminProfileService
    )

    @Test
    fun `authenticateStudent uses web TTL and sets auth cookie when mobile client is false`() {
        val response = mockk<HttpServletResponse>()
        val cookieSlot = slot<String>()
        every { response.setHeader(eq(HttpHeaders.SET_COOKIE), capture(cookieSlot)) } just Runs

        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val exp = Date.from(Instant.now().plusSeconds(3600))
        val token = JWT.create().withExpiresAt(exp).sign(Algorithm.HMAC256(jwtProperties.secret))

        val durationSlot = slot<Duration>()
        every {
            jwtTokenUtil.createToken(
                eq(studentId),
                eq(UserType.STUDENT),
                capture(durationSlot)
            )
        } returns JwtTokenData(
            token = token,
            expiresAt = exp.time
        )

        val result =
            service.authenticateStudent(studentId, response, mobileAppClient = false, currentClassroomId = classroomId)

        assertEquals(jwtProperties.ttlWeb, durationSlot.captured)
        assertNotNull(result)
        assertEquals(studentId, result.currentUser.id)
        assertEquals(UserType.STUDENT.name, result.currentUser.userType)
        assertEquals(classroomId, result.currentClassroomId)

        val cookie = cookieSlot.captured
        assertTrue(cookie.contains("Authorization="))
        assertTrue(cookie.contains("Bearer+")) // space encoded as '+'
        assertTrue(cookie.contains("HttpOnly"))
        assertTrue(cookie.contains("Secure"))
        assertTrue(cookie.contains("SameSite=None"))

        verify(exactly = 1) { jwtTokenUtil.createToken(studentId, UserType.STUDENT, any()) }
        confirmVerified(jwtTokenUtil)
    }

    @Test
    fun `authenticateStudent uses mobile TTL and sets auth cookie when mobile client is true`() {
        val response = mockk<HttpServletResponse>()
        val cookieSlot = slot<String>()
        every { response.setHeader(eq(HttpHeaders.SET_COOKIE), capture(cookieSlot)) } just Runs

        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val exp = Date.from(Instant.now().plusSeconds(7200))
        val token = JWT.create().withExpiresAt(exp).sign(Algorithm.HMAC256(jwtProperties.secret))

        val durationSlot = slot<Duration>()
        every {
            jwtTokenUtil.createToken(
                eq(studentId),
                eq(UserType.STUDENT),
                capture(durationSlot)
            )
        } returns JwtTokenData(
            token = token,
            expiresAt = exp.time
        )

        val result =
            service.authenticateStudent(studentId, response, mobileAppClient = true, currentClassroomId = classroomId)

        assertEquals(jwtProperties.ttlMobile, durationSlot.captured)
        assertNotNull(result)
        assertEquals(studentId, result.currentUser.id)
        assertEquals(UserType.STUDENT.name, result.currentUser.userType)
        assertEquals(classroomId, result.currentClassroomId)

        val cookie = cookieSlot.captured
        assertTrue(cookie.contains("Authorization="))
        assertTrue(cookie.contains("Bearer+"))
        assertTrue(cookie.contains("HttpOnly"))
        assertTrue(cookie.contains("Secure"))
        assertTrue(cookie.contains("SameSite=None"))

        verify(exactly = 1) { jwtTokenUtil.createToken(studentId, UserType.STUDENT, any()) }
        confirmVerified(jwtTokenUtil)
    }
}

