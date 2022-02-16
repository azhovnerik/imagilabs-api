package com.anahoret.imagilabsapi.auth.web.jwt

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class JwtUser(
    val id: UUID,
    private val username: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
    private val active: Boolean,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getUsername(): String {
        return username
    }

    val userType = when {
        authorities.any { it.authority == "ROLE_TEACHER" } -> {
            JwtUserType.TEACHER
        }
        authorities.any { it.authority == "ROLE_STUDENT" } -> {
            JwtUserType.STUDENT
        }
        else -> throw UnknownUserTypeException()
    }

    @JsonIgnore
    override fun isEnabled(): Boolean {
        return active
    }

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    @JsonIgnore
    override fun getPassword(): String {
        return password
    }

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean {
        return true
    }

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JwtUser) return false

        if (id != other.id) return false
        if (username != other.username) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        return result
    }

    inner class UnknownUserTypeException :
        RuntimeException("No user type for roles ${authorities.joinToString { it.authority }}} ")

}
