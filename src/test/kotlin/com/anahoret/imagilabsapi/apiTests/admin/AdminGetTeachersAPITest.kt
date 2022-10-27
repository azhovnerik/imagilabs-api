package com.anahoret.imagilabsapi.apiTests.admin

import com.anahoret.imagilabsapi.auth.web.RequestAuthenticatorService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.web.TeacherProfileController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When admin tries to fetch teachers")
class AdminGetTeachersAPITest (
    @Autowired private val mockMvc: MockMvc
) {

    @MockBean
    lateinit var requestAuthenticatorService: RequestAuthenticatorService

    @MockBean
    lateinit var teacherProfileService: TeacherProfileService

    @MockBean
    lateinit var teacherProfileController: TeacherProfileController

    @Test
    fun `should return success`() {
        val teacherId = UUID.randomUUID()
        mockMvc.perform(get("/api/teachers/$teacherId")
            .with(user("admin@gmail.com").password("password1").roles("ADMIN"))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
