package com.anahoret.imagilabsapi.auth.util

import com.anahoret.imagilabsapi.auth.web.jwt.JwtProperties
import com.auth0.jwt.interfaces.DecodedJWT
import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.getJWT(): DecodedJWT? {
    return getAttribute(JwtProperties.JWT_REQUEST_ATTRIBUTE) as? DecodedJWT
}
