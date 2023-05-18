package com.anahoret.imagilabsapi.auth.domain

import com.anahoret.imagilabsapi.admins.domain.AdminProfile
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
            is AdminProfile -> principal.id
            else -> null
        }
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun getCredentials(): Any? {
        return credentials
    }

    override fun getPrincipal(): Any {
        return principal
    }

    companion object {

        private const val serialVersionUID: Long = 686465555891653461L
    }
}
