package com.anahoret.imagilabsapi.security

import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service

interface AuthorityService {

    fun getAuthorities(teacherProfile: TeacherProfile): Collection<GrantedAuthority>
}

@Service
class AuthorityServiceImpl : AuthorityService {

    override fun getAuthorities(teacherProfile: TeacherProfile): Collection<GrantedAuthority> {
        return when {
            teacherProfile.emailVerified -> listOf(SimpleGrantedAuthority(UserRole.teacher))
            else -> listOf(SimpleGrantedAuthority(UserRole.teacherEmailNotVerified))
        }
    }

}
