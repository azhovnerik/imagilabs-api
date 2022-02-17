package com.anahoret.imagilabsapi.auth.domain

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class ImagiLabsAuthentication(
    private val principal: Any,
    authorities: Collection<GrantedAuthority>
) : AbstractAuthenticationToken(authorities) {

    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): Any {
        return principal
    }
}
