package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.auth.web.jwt.JwtProperties
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.auth.web.jwt.JwtUser
import com.auth0.jwt.JWT
import org.apache.tomcat.util.http.SameSiteCookies
import org.springframework.http.HttpCookie
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import javax.servlet.http.HttpServletResponse

interface RequestAuthenticatorService {

    fun authenticate(
        userName: String,
        response: HttpServletResponse,
        mobileAppClient: Boolean
    ): AuthenticationResponse

    fun updateAuthenticationToken(
        authToken: String,
        response: HttpServletResponse
    )

    fun logout(response: HttpServletResponse)
}

@Service
class RequestAuthenticatorServiceImpl(
    private val userDetailsService: UserDetailsService,
    private val jwtTokenUtil: JwtTokenUtil,
    private val jwtProperties: JwtProperties
) : RequestAuthenticatorService {

    override fun authenticate(
        userName: String,
        response: HttpServletResponse,
        mobileAppClient: Boolean
    ): AuthenticationResponse {
        val userDetails = userDetailsService.loadUserByUsername(userName) as JwtUser
        val tokenTTL = getTokenTTL(mobileAppClient)
        val (token, expiresAt) = jwtTokenUtil.createToken(userDetails, tokenTTL)
        setAuthCookie(token, response)
        val userData = UserData(userDetails.id, userDetails.userType.name, expiresAt, profile = null)
        return AuthenticationSuccess(userData, token)
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
