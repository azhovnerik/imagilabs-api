package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.admins.domain.AdminProfileService
import com.anahoret.imagilabsapi.auth.web.jwt.JwtProperties
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import com.auth0.jwt.JWT
import org.apache.tomcat.util.http.SameSiteCookies
import org.springframework.http.HttpCookie
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import java.util.*
import jakarta.servlet.http.HttpServletResponse

interface RequestAuthenticatorService {

    fun authenticateStudent(
        studentId: UUID,
        response: HttpServletResponse,
        mobileAppClient: Boolean,
        currentClassroomId: UUID
    ): StudentAuthenticationSuccess

    fun authenticateTeacher(
        teacherId: UUID,
        response: HttpServletResponse,
        mobileAppClient: Boolean
    ): TeacherAuthenticationSuccess

    fun authenticateAdmin(
        adminId: UUID,
        response: HttpServletResponse,
        mobileAppClient: Boolean
    ): AdminAuthenticationSuccess

    fun updateAuthenticationToken(
        authToken: String,
        response: HttpServletResponse
    )

    fun logout(response: HttpServletResponse)
}

@Service
class RequestAuthenticatorServiceImpl(
    private val jwtTokenUtil: JwtTokenUtil,
    private val jwtProperties: JwtProperties,
    private val teacherProfileService: TeacherProfileService,
    private val studentProfileService: StudentProfileService,
    private val adminProfileService: AdminProfileService,
) : RequestAuthenticatorService {

    override fun authenticateStudent(
        studentId: UUID,
        response: HttpServletResponse,
        mobileAppClient: Boolean,
        currentClassroomId: UUID
    ): StudentAuthenticationSuccess {
        val tokenTTL = getTokenTTL(mobileAppClient)
        val jwtTokenData = jwtTokenUtil.createToken(studentId, UserType.STUDENT, tokenTTL)
        setAuthCookie(jwtTokenData.token, response)
        val profile = studentProfileService.getStudentById(studentId)?.let(::StudentUserProfileData)
        val userData = StudentUserData(studentId, UserType.STUDENT.name, profile, currentClassroomId)
        return StudentAuthenticationSuccess(userData, jwtTokenData)
    }

    override fun authenticateTeacher(
        teacherId: UUID,
        response: HttpServletResponse,
        mobileAppClient: Boolean
    ): TeacherAuthenticationSuccess {
        val tokenTTL = getTokenTTL(mobileAppClient)
        val jwtTokenData = jwtTokenUtil.createToken(teacherId, UserType.TEACHER, tokenTTL)
        setAuthCookie(jwtTokenData.token, response)
        val profile = teacherProfileService.getTeacherById(teacherId)?.let(::TeacherUserProfileData)
        val userData = TeacherUserData(teacherId, UserType.TEACHER.name, profile)
        return TeacherAuthenticationSuccess(userData, jwtTokenData)
    }

    override fun authenticateAdmin(
        adminId: UUID,
        response: HttpServletResponse,
        mobileAppClient: Boolean
    ): AdminAuthenticationSuccess {
        val tokenTTL = getTokenTTL(mobileAppClient)
        val jwtTokenData = jwtTokenUtil.createToken(adminId, UserType.ADMIN, tokenTTL)
        setAuthCookie(jwtTokenData.token, response)
        val profile = adminProfileService.getAdminById(adminId)?.let(::AdminUserProfileData)
        val userData = AdminUserData(adminId, UserType.ADMIN.name, profile)
        return AdminAuthenticationSuccess(userData, jwtTokenData)
    }

    override fun updateAuthenticationToken(
        authToken: String,
        response: HttpServletResponse
    ) {
        setAuthCookie(authToken, response)
    }

    private fun setAuthCookie(token: String, response: HttpServletResponse) {
        val maxAge = getMaxAgeFromJwt(token)
        val cookie = createAuthorizationCookie(URLEncoder.encode("Bearer $token", "UTF-8"), maxAge)
        response.setHttpCookie(cookie)
    }

    override fun logout(response: HttpServletResponse) {
        val cookie = createAuthorizationCookie("", 0)
        response.setHttpCookie(cookie)
    }

    private fun getTokenTTL(mobileAppClient: Boolean): Duration {
        return if (mobileAppClient) jwtProperties.ttlMobile else jwtProperties.ttlWeb
    }

    private fun HttpServletResponse.setHttpCookie(cookie: HttpCookie) {
        setHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private fun getMaxAgeFromJwt(token: String): Int {
        return Duration.between(
            Instant.now(),
            JWT.decode(token).expiresAt.toInstant()
        ).seconds.toInt()
    }

    private fun createAuthorizationCookie(value: String, maxAge: Int): HttpCookie {
        return ResponseCookie.from(HttpHeaders.AUTHORIZATION, value)
            .httpOnly(true)
            .maxAge(maxAge.toLong())
            .path("/")
            .secure(true)
            .sameSite(SameSiteCookies.NONE.value)
            .build()
    }

}
