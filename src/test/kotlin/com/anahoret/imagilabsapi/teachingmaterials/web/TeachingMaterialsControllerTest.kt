package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
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
        lateinit var classroomTeachingMaterialsGetUseCase: ClassroomTeachingMaterialsGetUseCase

        @MockkBean
        lateinit var teachingMaterialsGetUseCase: TeachingMaterialsGetUseCase

        private val teachingMaterials = teachingMaterials()

        @Test
        fun `should return success for teacher`() {
            val teacherProfile = testTeacher()

            every { teachingMaterialsGetUseCase.get(teacherProfile.id) } returns teachingMaterials

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teachingMaterialsGetUseCase.get(teacherProfile.id) }
        }

        @Test
        fun `should return forbidden error`() {
            val studentProfile = testStudent()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent(studentProfile)
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when getting classroom teaching materials")
    @Nested
    @WebMvcTest(
        TeachingMaterialController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class ClassroomTeachingMaterialsTest: ControllerTest() {

        @MockkBean
        lateinit var classroomTeachingMaterialsGetUseCase: ClassroomTeachingMaterialsGetUseCase

        @MockkBean
        lateinit var teachingMaterialsGetUseCase: TeachingMaterialsGetUseCase

        private val teachingMaterials = teachingMaterials()

        @Test
        fun `should return success for teacher`() {
            val classroomId = UUID.randomUUID()
            val teacherProfile = testTeacher()

            every { classroomTeachingMaterialsGetUseCase.get(teacherProfile, classroomId) } returns teachingMaterials.right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/classrooms/$classroomId/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { classroomTeachingMaterialsGetUseCase.get(teacherProfile, classroomId) }
        }

        @Test
        fun `should return access denied error for teacher`() {
            val classroomId = UUID.randomUUID()
            val teacherProfile = testTeacher()

            every { classroomTeachingMaterialsGetUseCase.get(teacherProfile, classroomId) } returns AccessDeniedError("").left()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/classrooms/$classroomId/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isForbidden)

            verify { classroomTeachingMaterialsGetUseCase.get(teacherProfile, classroomId) }
        }

        @Test
        fun `should return success for student`() {
            val classroomId = UUID.randomUUID()
            val studentProfile = testStudent(classroomId)

            every { classroomTeachingMaterialsGetUseCase.get(studentProfile, classroomId) } returns teachingMaterials.right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/classrooms/$classroomId/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent(studentProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { classroomTeachingMaterialsGetUseCase.get(studentProfile, classroomId) }
        }

        @Test
        fun `should return access denied error for student`() {
            val classroomId = UUID.randomUUID()
            val studentProfile = testStudent(classroomId)

            every { classroomTeachingMaterialsGetUseCase.get(studentProfile, classroomId) } returns AccessDeniedError("").left()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/classrooms/$classroomId/teaching-materials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent(studentProfile)
            ).andExpect(MockMvcResultMatchers.status().isForbidden)

            verify { classroomTeachingMaterialsGetUseCase.get(studentProfile, classroomId) }
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
