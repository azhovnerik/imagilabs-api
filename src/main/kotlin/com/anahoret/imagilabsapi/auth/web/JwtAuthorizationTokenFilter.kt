package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.auth.domain.ImagiLabsAuthenticationToken
import com.anahoret.imagilabsapi.auth.web.jwt.JwtProperties
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.auth.web.jwt.getToken
import com.anahoret.imagilabsapi.auth.web.jwt.getUserType
import com.anahoret.imagilabsapi.security.AuthorityService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthorizationTokenFilter(
    private val teacherProfileService: TeacherProfileService,
    private val studentProfileService: StudentProfileService,
    private val authorityService: AuthorityService,
    private val jwtTokenUtil: JwtTokenUtil
) : OncePerRequestFilter() {

    companion object {

        const val JWT_TOKEN_HEADER_NAME = "Authorization"
        const val JWT_TOKEN_HEADER_PREFIX = "Bearer"
    }

    private val antMatcher = AntPathMatcher()
    private val ignorePaths = setOf(
        "/api/auth/teacher",
        "/api/auth/student",
        "/api/auth/logout",
        "/api/sign-up/teacher"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        request.getToken()
            ?.let { jwtTokenUtil.verifyAndDecode(it) }
            ?.also { request.setAttribute(JwtProperties.JWT_REQUEST_ATTRIBUTE, it) }
            ?.let {
                val securityContext = SecurityContextHolder.getContext()
                if (securityContext.authentication == null) {
                    authenticateUser(securityContext, it.getUserType(), UUID.fromString(it.subject), request)
                }
            }

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return ignorePaths.any { antMatcher.match(it, request.requestURI) }
    }

    private fun authenticateUser(
        securityContext: SecurityContext,
        userType: UserType?,
        userId: UUID,
        request: HttpServletRequest
    ) {
        if (userType == null) {
            logger.error("Cannot authenticate user. User type missing in JWT.")
            return
        }

        val userProfile: Any? = when (userType) {
            UserType.TEACHER -> teacherProfileService.getTeacherById(userId)
            UserType.STUDENT -> studentProfileService.getStudentById(userId)
        }

        val authorities = when (userProfile) {
            is TeacherProfile -> authorityService.getAuthorities(userProfile)
            is StudentProfile -> authorityService.getAuthorities(userProfile)

            null -> {
                logger.error("Cannot authenticate user. User not found.")
                return
            }

            else -> {
                logger.error("Cannot authenticate user. User type unknown.")
                return
            }
        }

        val authentication = ImagiLabsAuthenticationToken(userProfile, userType, authorities, credentials = null)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        securityContext.authentication = authentication
    }

}
