package com.anahoret.imagilabsapi.apiTests.teacher

import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import com.anahoret.imagilabsapi.teachers.web.TeacherProfileController
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ImagiLabsTestPropertySource
@DisplayName("When teacher tries to delete their account")
class TeacherDeleteAccountAPITest (
    @Autowired private val mockMvc: MockMvc

) {
    @Autowired
    lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

    @MockitoBean
    lateinit var teacherProfileController: TeacherProfileController

    private lateinit var teacherProfileEntity: TeacherProfileEntity
    private lateinit var teacherId: UUID

    @BeforeEach
    fun setup() {
        setupTeacher()
    }

    private fun setupTeacher() {
        teacherProfileEntity = teacherProfileEntityRepository.save(
            TeacherProfileEntity(
                email = "teacher2@mail.com",
                passwordHash = "",
                firstName = "Teacher",
                lastName = "Edu",
                country = "Sweden",
                organization = "imagi",
                howDidYouHearAboutUs = "other",
                howDidYouHearAboutUsOther = "other way",
                marketingEmailSubscribed = true,
                tipTokens = 3,
                tipTokensReplenishedAt = 0
            )
        )
        teacherId = teacherProfileEntity.id!!
    }

    @Test
    fun `account is deleted successfully`() {
        mockMvc.perform(delete("/api/teachers/$teacherId")
            .with(user("teacher2@gmail.com").password("password1").roles("TEACHER"))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
