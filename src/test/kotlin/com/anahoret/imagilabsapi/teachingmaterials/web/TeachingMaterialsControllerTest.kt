package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.domain.error.UnsupportedUserTypeError
import com.anahoret.imagilabsapi.common.testAdmin
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterials
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterialsGetUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@DisplayName("Teaching materials controller")
class TeachingMaterialsControllerTest {

    @ExtendWith(SpringExtension::class)
    @DisplayName("when getting teaching materials")
    @Nested
    @WebMvcTest(
        TeachingMaterialController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class ListNewMaterialsTest : ControllerTest() {

        @MockkBean
        lateinit var teachingMaterialsGetUseCase: TeachingMaterialsGetUseCase

        private val teachingMaterials = TeachingMaterials.empty()

        @Test
        fun `should return success for teacher`() {
            val teacherProfile = testTeacher()

            every { teachingMaterialsGetUseCase.get(teacherProfile) } returns teachingMaterials.right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teachingMaterialsGetUseCase.get(teacherProfile) }
        }

        @Test
        fun `should return unsupported user type error for admin`() {
            val adminProfile = testAdmin()

            every { teachingMaterialsGetUseCase.get(adminProfile) } returns UnsupportedUserTypeError.left()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin(adminProfile)
            ).andExpect(MockMvcResultMatchers.status().isForbidden)

            verify(inverse = true) { teachingMaterialsGetUseCase.get(adminProfile) }
        }

        @Test
        fun `should return success for student`() {
            val studentProfile = testStudent()

            every { teachingMaterialsGetUseCase.get(studentProfile) } returns teachingMaterials.right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent(studentProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teachingMaterialsGetUseCase.get(studentProfile) }
        }
    }

}
