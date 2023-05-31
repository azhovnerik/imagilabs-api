package com.anahoret.imagilabsapi.common

import com.anahoret.imagilabsapi.admins.domain.AdminProfile
import com.anahoret.imagilabsapi.admins.domain.AdminProfileService
import com.anahoret.imagilabsapi.auth.web.jwt.JwtConfig
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.security.AuthorityService
import com.anahoret.imagilabsapi.security.SecurityConfig
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import java.util.*

@ExtendWith(SpringExtension::class)
@ImagiLabsTestPropertySource
@Import(SecurityConfig::class, JwtConfig::class)
abstract class ControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var jwtTokenUtil: JwtTokenUtil

    @MockkBean
    lateinit var teacherProfileService: TeacherProfileService

    @MockkBean
    lateinit var studentProfileService: StudentProfileService

    @MockkBean
    lateinit var adminProfileService: AdminProfileService

    @MockkBean
    lateinit var authorityService: AuthorityService

    @MockkBean
    lateinit var logger: Logger

    fun MockHttpServletRequestBuilder.asAdmin(adminProfile: AdminProfile = testAdmin()): MockHttpServletRequestBuilder {
        every { adminProfileService.getAdminById(adminProfile.id) } returns adminProfile
        every { authorityService.getAuthorities(adminProfile) } returns listOf(SimpleGrantedAuthority(UserRole.admin))
        val token = jwtTokenUtil.createToken(adminProfile.id, UserType.ADMIN)
        return this.header("Authorization", "Bearer+${token.token}")
    }

    fun MockHttpServletRequestBuilder.asTeacher(teacherProfile: TeacherProfile = testTeacher()): MockHttpServletRequestBuilder {
        every { teacherProfileService.getTeacherById(teacherProfile.id) } returns teacherProfile
        every { authorityService.getAuthorities(teacherProfile) } returns listOf(SimpleGrantedAuthority(UserRole.teacher))
        val token = jwtTokenUtil.createToken(teacherProfile.id, UserType.TEACHER)
        return this.header("Authorization", "Bearer+${token.token}")
    }

    fun MockHttpServletRequestBuilder.asStudent(studentProfile: StudentProfile = testStudent()): MockHttpServletRequestBuilder {
        every { studentProfileService.getStudentById(studentProfile.id) } returns studentProfile
        every { authorityService.getAuthorities(studentProfile) } returns listOf(SimpleGrantedAuthority(UserRole.student))
        val token = jwtTokenUtil.createToken(studentProfile.id, UserType.STUDENT)
        return this.header("Authorization", "Bearer+${token.token}")
    }

}
