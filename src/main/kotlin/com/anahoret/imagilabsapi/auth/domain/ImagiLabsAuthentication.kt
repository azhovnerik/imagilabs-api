package com.anahoret.imagilabsapi.auth.domain

import com.anahoret.imagilabsapi.users.UserType
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class ImagiLabsAuthentication(
    private val principal: Any,
    val userType: UserType,
    authorities: Collection<GrantedAuthority>? = null,
    private val credentials: Any? = null
) : AbstractAuthenticationToken(authorities) {

    constructor(principal: Any, userType: UserType, credentials: Any) : this(
        principal,
        userType,
        authorities = null,
        credentials
    )

    override fun getCredentials(): Any? {
        return credentials
    }

    override fun getPrincipal(): Any {
        return principal
    }
}
