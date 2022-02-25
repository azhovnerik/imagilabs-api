package com.anahoret.imagilabsapi.auth.domain

import com.anahoret.imagilabsapi.security.AuthorityService
import com.anahoret.imagilabsapi.students.domain.StudentLoginRequest
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class ImagiLabsAuthenticationManager(
    private val teacherService: TeacherProfileService,
    private val studentProfileService: StudentProfileService,
    private val passwordEncoder: PasswordEncoder,
    private val authorityService: AuthorityService
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication?): Authentication {
        if (authentication == null) throw BadCredentialsException("Authentication is null")
        if (authentication !is ImagiLabsAuthenticationToken) throw BadCredentialsException("Invalid authentication type")
        val principal = authentication.principal
        if (principal !is String) return authentication
        val credentials = authentication.credentials ?: throw BadCredentialsException("Password is null")
        if (credentials !is String && credentials !is StudentLoginRequest)
            throw BadCredentialsException("Password should be string")

        return when (authentication.userType) {
            UserType.TEACHER -> {
                val password = credentials as? String
                    ?: throw BadCredentialsException("Teacher password should be String")
                authenticateTeacher(principal, password)
            }
            UserType.STUDENT -> {
                val studentLoginRequest = credentials as? StudentLoginRequest
                    ?: throw BadCredentialsException("Student credentials should be StudentLoginRequest")
                authenticateStudent(studentLoginRequest)
            }
        }
    }

    private fun authenticateTeacher(email: String, password: String): Authentication {
        val teacherCredentials = teacherService.getTeacherCredentialsByEmail(email)
            ?: throw BadCredentialsException("Username or password is incorrect")
        if (passwordEncoder.matches(password, teacherCredentials.passwordHash)) {
            val teacherProfile = teacherService.getTeacherById(teacherCredentials.id)
                ?: throw BadCredentialsException("Account does not exist")
            val authorities = authorityService.getAuthorities(teacherProfile)
            return ImagiLabsAuthenticationToken(teacherProfile, UserType.TEACHER, authorities)
        } else {
            throw BadCredentialsException("Username or password is incorrect")
        }
    }

    private fun authenticateStudent(studentLoginRequest: StudentLoginRequest): Authentication {
        val studentProfile = studentProfileService.getStudentCredentials(studentLoginRequest)
            ?.let { studentProfileService.getStudentById(it.id) }
            ?: throw BadCredentialsException("Credentials are incorrect")
        val authorities = authorityService.getAuthorities(studentProfile)
        return ImagiLabsAuthenticationToken(studentProfile, UserType.STUDENT, authorities)
    }
}
