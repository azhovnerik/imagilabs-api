package com.anahoret.imagilabsapi.apiTests.student

import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import com.anahoret.imagilabsapi.students.web.StudentController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@ImagiLabsTestPropertySource
@DisplayName("When student tries to fetch profile")
class StudentProfileAPITest (
    @Autowired private val mockMvc: MockMvc

) {

    @MockitoBean
    lateinit var studentController: StudentController

    @Test
    fun `should return success`() {
        mockMvc.perform(get("/api/student/profile/me")
            .with(user("Ann").password("password1").roles("STUDENT"))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
