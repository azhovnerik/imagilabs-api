package com.anahoret.imagilabsapi.apiTests.admin

import arrow.core.right
import com.anahoret.imagilabsapi.admins.domain.AdminProfile
import com.anahoret.imagilabsapi.admins.domain.AdminProfileService
import com.anahoret.imagilabsapi.auth.web.jwt.JwtConfig
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.security.AuthorityService
import com.anahoret.imagilabsapi.security.SecurityConfig
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherDeleteUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherGetUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileAdminView
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.web.TeacherProfileController
import com.anahoret.imagilabsapi.users.UserType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@ExtendWith(SpringExtension::class)
@WebMvcTest(
    TeacherProfileController::class,
    PasswordEncoder::class,
    AuthenticationEntryPoint::class,
    SecurityConfig::class,
    JwtTokenUtil::class,
    JwtConfig::class
)
@AutoConfigureMockMvc
@DisplayName("When admin tries to fetch teacher by id")
@ImagiLabsTestPropertySource
class AdminGetTeacherByIdAPITest {

    @Autowired
    lateinit var jwtTokenUtil: JwtTokenUtil

    @Autowired
    lateinit var mockMvc: MockMvc

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

    @MockkBean
    lateinit var teacherGetUseCase: TeacherGetUseCase

    @MockkBean
    lateinit var teacherDeleteUseCase: TeacherDeleteUseCase

    @Test
    fun `should return success`() {
        val teacherId = UUID.randomUUID()
        val adminId = UUID.randomUUID()
        every { teacherGetUseCase.get(teacherId) } returns mockk<TeacherProfileAdminView>(relaxed = true).right()
        val adminMock = AdminProfile(adminId, "Admin")
        every { adminProfileService.getAdminById(adminId) } returns adminMock
        every { authorityService.getAuthorities(adminMock) } returns listOf(SimpleGrantedAuthority(UserRole.admin))
        val token = jwtTokenUtil.createToken(adminId, UserType.ADMIN)
        mockMvc.perform(
            get("/api/teachers/$teacherId")
                .header("Authorization", "Bearer+${token.token}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
