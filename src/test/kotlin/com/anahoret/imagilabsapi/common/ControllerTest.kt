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

    fun MockHttpServletRequestBuilder.asAdmin(): MockHttpServletRequestBuilder {
        val userId = UUID.randomUUID()
        val adminProfile = AdminProfile(userId, "Admin")
        every { adminProfileService.getAdminById(userId) } returns adminProfile
        every { authorityService.getAuthorities(adminProfile) } returns listOf(SimpleGrantedAuthority(UserRole.admin))
        val token = jwtTokenUtil.createToken(userId, UserType.ADMIN)
        return this.header("Authorization", "Bearer+${token.token}")
    }

    fun MockHttpServletRequestBuilder.asTeacher(): MockHttpServletRequestBuilder {
        val userId = UUID.randomUUID()
        val teacherProfile = TeacherProfile(
            id = userId,
            firstName = "John",
            lastName = "Snow",
            email = "johnsnow@winterfell.com",
            country = "Westeros",
            organization = "Starks",
            createdAt = 0L,
            emailVerified = true,
            marketingEmailSubscribed = false,
            subscription = TeacherSubscription(null, null, TeacherSubscriptionPlan.STANDARD, false)
        )
        every { teacherProfileService.getTeacherById(userId) } returns teacherProfile
        every { authorityService.getAuthorities(teacherProfile) } returns listOf(SimpleGrantedAuthority(UserRole.teacher))
        val token = jwtTokenUtil.createToken(userId, UserType.TEACHER)
        return this.header("Authorization", "Bearer+${token.token}")
    }

    fun MockHttpServletRequestBuilder.asStudent(): MockHttpServletRequestBuilder {
        val userId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val studentProfile = StudentProfile(userId, "Aria", "aria", 0L, classroomId)
        every { studentProfileService.getStudentById(userId) } returns studentProfile
        every { authorityService.getAuthorities(studentProfile) } returns listOf(SimpleGrantedAuthority(UserRole.student))
        val token = jwtTokenUtil.createToken(userId, UserType.STUDENT)
        return this.header("Authorization", "Bearer+${token.token}")
    }

}
