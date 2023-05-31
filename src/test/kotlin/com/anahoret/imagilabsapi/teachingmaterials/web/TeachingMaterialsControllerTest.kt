package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.teachingmaterials.domain.*
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterialCategory.TEACHING_SLIDES
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterialCategory.WORKSHEETS
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
import java.util.*

@DisplayName("Teaching materials controller")
class TeachingMaterialsControllerTest {

    @ExtendWith(SpringExtension::class)
    @DisplayName("when setting subscription period")
    @Nested
    @WebMvcTest(
        TeachingMaterialController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    inner class ListNewMaterialsTest : ControllerTest() {

        @MockkBean
        lateinit var teachingMaterialsGetUseCase: TeachingMaterialsGetUseCase

        private val teachingMaterials = teachingMaterials()

        @Test
        fun `get list of materials by teacher`() {
            val classroomId = UUID.randomUUID()
            val teacherProfile = testTeacher()

            every { teachingMaterialsGetUseCase.get(teacherProfile, classroomId) } returns teachingMaterials.right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("classroomId", classroomId.toString())
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teachingMaterialsGetUseCase.get(teacherProfile, classroomId) }
        }

        @Test
        fun `get list of materials by student`() {
            val classroomId = UUID.randomUUID()
            val studentProfile = testStudent(classroomId)

            every { teachingMaterialsGetUseCase.get(studentProfile, classroomId) } returns teachingMaterials.right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("classroomId", classroomId.toString())
                    .asStudent(studentProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teachingMaterialsGetUseCase.get(studentProfile, classroomId) }
        }
    }

    private fun teachingMaterials(): TeachingMaterials {
        return TeachingMaterials(
            listOf(
                TeachingMaterial(
                UUID.randomUUID(), 0, "", "", true, TEACHING_SLIDES, false
            )),
            listOf(TeachingMaterial(
                UUID.randomUUID(), 0, "", "", true, WORKSHEETS, false
            )),
        )
    }
}
