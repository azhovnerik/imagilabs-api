package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.auth.web.jwt.JwtProperties
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.auth.web.jwt.JwtUser
import com.anahoret.imagilabsapi.auth.web.jwt.getToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthorizationTokenFilter(
    private val userDetailsService: UserDetailsService,
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
                    logger.debug("security context was null, so authenticating user")
                    authenticateUser(securityContext, it.subject, request)
                }
            }

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return ignorePaths.any { antMatcher.match(it, request.requestURI) }
    }

    private fun authenticateUser(securityContext: SecurityContext, username: String, request: HttpServletRequest) {
        val userDetails = try {
            userDetailsService.loadUserByUsername(username)
                .takeIf { it.isEnabled && it.isAccountNonLocked && it.isAccountNonExpired && it.isCredentialsNonExpired }
                ?.let { it as? JwtUser }

        } catch (e: UsernameNotFoundException) {
            logger.error("Cannot authenticate user. Username not found.")
            null
        } ?: return
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        logger.debug("authorized user '$username', setting security context")
        securityContext.authentication = authentication
    }

}
