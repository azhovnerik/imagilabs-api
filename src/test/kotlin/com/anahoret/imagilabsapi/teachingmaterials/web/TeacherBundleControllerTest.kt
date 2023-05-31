package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.testAdmin
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundle
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherBundleCreateUseCase
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherBundleDeleteUseCase
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherBundlesGetUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.json.JSONObject
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

@DisplayName("Teacher bundle controller")
class TeacherBundleControllerTest {

    @ExtendWith(SpringExtension::class)
    @DisplayName("when adding bundle to teacher")
    @Nested
    @WebMvcTest(
        TeacherBundleController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class AddBundleToTeacherTest: ControllerTest() {

        @MockkBean
        lateinit var teacherBundleCreateUseCase: TeacherBundleCreateUseCase

        @MockkBean
        lateinit var teacherBundlesGetUseCase: TeacherBundlesGetUseCase

        @MockkBean
        lateinit var teacherBundleDeleteUseCase: TeacherBundleDeleteUseCase

        private val bundleId = UUID.randomUUID()
        private val payload = JSONObject()
            .put("bundleId", bundleId)
            .toString()

        @Test
        fun `should return success when admin add bundle to teacher`() {
            val teacherId = UUID.randomUUID()

            every { teacherBundleCreateUseCase.create(teacherId, bundleId) } returns Unit.right()

            mvc.perform(
                MockMvcRequestBuilders.post("/api/teachers/$teacherId/bundles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teacherBundleCreateUseCase.create(teacherId, bundleId) }
        }

        @Test
        fun `should return not found error`() {
            val teacherId = UUID.randomUUID()

            every { teacherBundleCreateUseCase.create(teacherId, bundleId) } returns NotFoundError("").left()

            mvc.perform(
                MockMvcRequestBuilders.post("/api/teachers/$teacherId/bundles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            verify { teacherBundleCreateUseCase.create(teacherId, bundleId) }
        }

        @Test
        fun `should return forbidden error`() {
            val teacherId = UUID.randomUUID()

            mvc.perform(
                MockMvcRequestBuilders.post("/api/teachers/$teacherId/bundles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when adding bundle to teacher")
    @Nested
    @WebMvcTest(
        TeacherBundleController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class GetTeacherBundlesTest: ControllerTest() {

        @MockkBean
        lateinit var teacherBundleCreateUseCase: TeacherBundleCreateUseCase

        @MockkBean
        lateinit var teacherBundlesGetUseCase: TeacherBundlesGetUseCase

        @MockkBean
        lateinit var teacherBundleDeleteUseCase: TeacherBundleDeleteUseCase

        private val teacherId = UUID.randomUUID()

        @Test
        fun `should return teacher bundles by admin`() {
            val adminProfile = testAdmin()

            every { teacherBundlesGetUseCase.getAll(teacherId, adminProfile) } returns emptyList<LessonBundle>().right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teachers/$teacherId/bundles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin(adminProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teacherBundlesGetUseCase.getAll(teacherId, adminProfile) }
        }

        @Test
        fun `should return teacher bundles by teacher`() {
            val teacherProfile = testTeacher()

            every { teacherBundlesGetUseCase.getAll(teacherId, teacherProfile) } returns emptyList<LessonBundle>().right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teachers/$teacherId/bundles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teacherBundlesGetUseCase.getAll(teacherId, teacherProfile) }
        }

        @Test
        fun `should return teacher not found error`() {
            val teacherProfile = testTeacher()

            every { teacherBundlesGetUseCase.getAll(teacherId, teacherProfile) } returns NotFoundError("").left()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teachers/$teacherId/bundles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            verify { teacherBundlesGetUseCase.getAll(teacherId, teacherProfile) }
        }

        @Test
        fun `should return forbidden error`() {
            mvc.perform(
                MockMvcRequestBuilders.get("/api/teachers/$teacherId/bundles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when adding bundle to teacher")
    @Nested
    @WebMvcTest(
        TeacherBundleController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class DeleteTeacherBundleTest: ControllerTest() {

        @MockkBean
        lateinit var teacherBundleCreateUseCase: TeacherBundleCreateUseCase

        @MockkBean
        lateinit var teacherBundlesGetUseCase: TeacherBundlesGetUseCase

        @MockkBean
        lateinit var teacherBundleDeleteUseCase: TeacherBundleDeleteUseCase

        private val teacherBundleId = UUID.randomUUID()

        @Test
        fun `should return success`() {
            val adminProfile = testAdmin()

            every { teacherBundleDeleteUseCase.delete(teacherBundleId) } returns Unit.right()

            mvc.perform(
                MockMvcRequestBuilders.delete("/api/teachers/bundles/$teacherBundleId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin(adminProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teacherBundleDeleteUseCase.delete(teacherBundleId) }
        }

        @Test
        fun `should return not found error`() {
            val adminProfile = testAdmin()

            every { teacherBundleDeleteUseCase.delete(teacherBundleId) } returns NotFoundError("").left()

            mvc.perform(
                MockMvcRequestBuilders.delete("/api/teachers/bundles/$teacherBundleId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin(adminProfile)
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            verify { teacherBundleDeleteUseCase.delete(teacherBundleId) }
        }

        @Test
        fun `should return forbidden error`() {
            mvc.perform(
                MockMvcRequestBuilders.delete("/api/teachers/bundles/$teacherBundleId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }
    }
}
