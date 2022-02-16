package com.anahoret.imagilabsapi.auth.web.jwt

import com.anahoret.imagilabsapi.auth.web.JwtAuthorizationTokenFilter
import java.net.URLDecoder
import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.getToken(): String? {
    return getTokenFromHeader() ?: getTokenFromCookie()
}

private fun HttpServletRequest.getTokenFromHeader(): String? {
    return getHeader(JwtAuthorizationTokenFilter.JWT_TOKEN_HEADER_NAME)
        ?.takeIf { it.startsWith("${JwtAuthorizationTokenFilter.JWT_TOKEN_HEADER_PREFIX} ") }
        ?.substring(JwtAuthorizationTokenFilter.JWT_TOKEN_HEADER_PREFIX.length + 1)
}

private fun HttpServletRequest.getTokenFromCookie(): String? {
    return cookies?.find { it.name == JwtAuthorizationTokenFilter.JWT_TOKEN_HEADER_NAME }?.value
        ?.let { URLDecoder.decode(it, "UTF-8") }
        ?.takeIf { it.startsWith("${JwtAuthorizationTokenFilter.JWT_TOKEN_HEADER_PREFIX} ") }
        ?.substring(JwtAuthorizationTokenFilter.JWT_TOKEN_HEADER_PREFIX.length + 1)
}
