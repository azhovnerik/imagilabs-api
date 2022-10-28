package com.anahoret.imagilabsapi.apiTests.classroom

import com.anahoret.imagilabsapi.classrooms.web.ClassroomController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When teacher tries to create a classroom")
class ClassroomCreateAPITest (
    @Autowired private val mockMvc: MockMvc

) {
    @MockBean
    lateinit var classroomController: ClassroomController

    @Test
    fun `classroom is created successfully`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/classrooms")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"Class A\", \"studentNames\": \"Anna\\nPaul\\nJim\"}")
                .with(user("teacher@gmail.com").password("password1").roles("TEACHER"))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
