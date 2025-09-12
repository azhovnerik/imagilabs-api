package com.anahoret.imagilabsapi.apiTests.teacher

import com.anahoret.imagilabsapi.auth.web.AuthenticationController
import com.anahoret.imagilabsapi.auth.web.RequestAuthenticatorService
import com.anahoret.imagilabsapi.signup.domain.TeacherEmailVerificationCodeSenderUseCaseImpl
import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import com.anahoret.imagilabsapi.teachers.domain.TeacherPasswordResetCodeSenderUseCaseImpl
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.web.TeacherProfileController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@ImagiLabsTestPropertySource
@DisplayName("When teacher tries to fetch profile")
class TeacherProfileAPITest (
    @Autowired private val mockMvc: MockMvc

) {
    @MockitoBean
    lateinit var authenticationManager: AuthenticationManager

    @MockitoBean
    lateinit var authenticationController: AuthenticationController

    @MockitoBean
    lateinit var requestAuthenticatorService: RequestAuthenticatorService

    @MockitoBean
    lateinit var teacherProfileService: TeacherProfileService

    @MockitoBean
    lateinit var teacherProfileController: TeacherProfileController

    @MockitoBean
    lateinit var teacherEmailVerificationCodeSenderUseCaseImpl: TeacherEmailVerificationCodeSenderUseCaseImpl

    @MockitoBean
    lateinit var teacherPasswordResetCodeSenderUseCaseImpl: TeacherPasswordResetCodeSenderUseCaseImpl

    @Test
    fun `should return success`() {
        mockMvc.perform(get("/api/teacher/profile/me")
            .with(user("teacher@gmail.com").password("password1").roles("TEACHER"))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
