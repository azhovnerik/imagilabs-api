package com.anahoret.imagilabsapi.auth.domain

import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import java.util.*

class ImagiLabsAuthenticationToken(
    private val principal: Any,
    val userType: UserType,
    authorities: Collection<GrantedAuthority>? = null,
    private val credentials: Any?
) : AbstractAuthenticationToken(authorities) {

    constructor(principal: Any, userType: UserType, credentials: Any) : this(
        principal,
        userType,
        authorities = null,
        credentials
    )

    fun getPrincipalId(): UUID? {
        return when (principal) {
            is TeacherProfile -> principal.id
            is StudentProfile -> principal.id
            else -> null
        }
    }

    override fun getCredentials(): Any? {
        return credentials
    }

    override fun getPrincipal(): Any {
        return principal
    }
}
