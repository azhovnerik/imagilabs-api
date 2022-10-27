package com.anahoret.imagilabsapi.apiTests.admin

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When admin tries to fetch teachers")
class AdminGetTeachersAPITest (
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    fun `should return success`() {
        mockMvc.perform(get("/api/teachers")
            .with(user("admin@gmail.com").password("password1").roles("ADMIN"))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}