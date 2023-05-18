package com.anahoret.imagilabsapi.authTests

import com.anahoret.imagilabsapi.auth.web.AuthenticationController
import com.anahoret.imagilabsapi.auth.web.RequestAuthenticatorService
import com.anahoret.imagilabsapi.signup.domain.TeacherEmailVerificationCodeSenderUseCaseImpl
import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import com.anahoret.imagilabsapi.teachers.domain.TeacherPasswordResetCodeSenderUseCaseImpl
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@SpringBootTest
@AutoConfigureMockMvc
@ImagiLabsTestPropertySource
@DisplayName("When teacher tries to login")
class TeacherAuthAPITest (
    @Autowired private val mockMvc: MockMvc

) {
    @MockBean
    lateinit var authenticationManager: AuthenticationManager

    @MockBean
    lateinit var authenticationController: AuthenticationController

    @MockBean
    lateinit var requestAuthenticatorService: RequestAuthenticatorService

    @MockBean
    lateinit var teacherProfileService: TeacherProfileService

    @MockBean
    lateinit var teacherEmailVerificationCodeSenderUseCaseImpl: TeacherEmailVerificationCodeSenderUseCaseImpl

    @MockBean
    lateinit var teacherPasswordResetCodeSenderUseCaseImpl: TeacherPasswordResetCodeSenderUseCaseImpl

    @Test
    fun `should authenticate teacher successfully`() {
        mockMvc.perform(post("/api/auth/teacher")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\": \"teacher@email.com\", \"password\": \"imagilabs1\", \"mobileAppClient\": \"false\" }")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
