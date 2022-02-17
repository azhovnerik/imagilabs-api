package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.auth.domain.ImagiLabsAuthentication
import com.anahoret.imagilabsapi.auth.web.jwt.JwtProperties
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.auth.web.jwt.getToken
import com.anahoret.imagilabsapi.auth.web.jwt.getUserType
import com.anahoret.imagilabsapi.security.AuthorityService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherService
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
    private val teacherService: TeacherService,
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
        "/api/auth/logout"
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

        val userProfile = when (userType) {
            UserType.TEACHER -> teacherService.getTeacherById(userId)
            UserType.STUDENT -> TODO()
        }

        val authorities = when (userProfile) {
            is TeacherProfile -> authorityService.getAuthorities(userProfile)

            null -> {
                logger.error("Cannot authenticate user. User not found.")
                return
            }

            else -> {
                logger.error("Cannot authenticate user. User type unknown.")
                return
            }
        }

        val authentication = ImagiLabsAuthentication(userProfile, userType, authorities = authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        securityContext.authentication = authentication
    }

}
