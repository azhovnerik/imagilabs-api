package com.anahoret.imagilabsapi.security

import com.anahoret.imagilabsapi.admins.domain.AdminProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service

interface AuthorityService {

    fun getAuthorities(teacherProfile: TeacherProfile): Collection<GrantedAuthority>
    fun getAuthorities(studentProfile: StudentProfile): Collection<GrantedAuthority>
    fun getAuthorities(adminProfile: AdminProfile): Collection<GrantedAuthority>
}

@Service
class AuthorityServiceImpl : AuthorityService {

    override fun getAuthorities(teacherProfile: TeacherProfile): Collection<GrantedAuthority> {
        return when {
            teacherProfile.emailVerified -> listOf(SimpleGrantedAuthority(UserRole.TEACHER))
            else -> listOf(SimpleGrantedAuthority(UserRole.TEACHER_EMAIL_NOT_VERIFIED))
        }
    }

    override fun getAuthorities(studentProfile: StudentProfile): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(UserRole.STUDENT))
    }

    override fun getAuthorities(adminProfile: AdminProfile): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(UserRole.ADMIN))
    }

}
