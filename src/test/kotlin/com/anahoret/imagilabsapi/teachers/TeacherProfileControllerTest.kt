package com.anahoret.imagilabsapi.teachers

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.teachers.domain.*
import com.anahoret.imagilabsapi.teachers.storage.TeacherStatistic
import com.anahoret.imagilabsapi.teachers.web.TeacherProfileController
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
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

@DisplayName("Teacher profile controller")
class TeacherProfileControllerTest {

    @ExtendWith(SpringExtension::class)
    @DisplayName("when get statistic")
    @Nested
    @WebMvcTest(
        TeacherProfileController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class GetStatistic : ControllerTest() {

        @MockkBean
        lateinit var teacherGetUseCase: TeacherGetUseCase

        @MockkBean
        lateinit var teacherDeleteUseCase: TeacherDeleteUseCase

        @MockkBean
        lateinit var teacherGetStatisticUseCase: TeacherGetStatisticUseCase

        @MockkBean
        lateinit var teacherProfileUpdateUseCase: TeacherProfileUpdateUseCase

        @Test
        fun `should return forbidden error when user isn't a teacher`(){
            mvc.perform(
                MockMvcRequestBuilders.get("/api/teacher/statistic")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return not found error when use case return error`() {
            val testTeacher = testTeacher()

            every { teacherGetStatisticUseCase.get(testTeacher.id) } returns NotFoundError("TEACHER_NOT_FOUND").left()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teacher/statistic")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            verify { teacherGetStatisticUseCase.get(testTeacher.id) }
        }

        @Test
        fun `should return success`() {
            val testTeacher = testTeacher()
            val teacherStatistic = mockk<TeacherStatistic> {
                every { activeClassrooms } returns 0
                every { studentAccounts } returns 0
                every { studentSharedProjects } returns 0
                every { studentDraftProjects } returns 0
            }.right()

            every { teacherGetStatisticUseCase.get(testTeacher.id) } returns teacherStatistic

            mvc.perform(
                MockMvcRequestBuilders.get("/api/teacher/statistic")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teacherGetStatisticUseCase.get(testTeacher.id) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when update profile")
    @Nested
    @WebMvcTest(
        TeacherProfileController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class UpdateProfile : ControllerTest() {

        @MockkBean
        lateinit var teacherGetUseCase: TeacherGetUseCase

        @MockkBean
        lateinit var teacherDeleteUseCase: TeacherDeleteUseCase

        @MockkBean
        lateinit var teacherGetStatisticUseCase: TeacherGetStatisticUseCase

        @MockkBean
        lateinit var teacherProfileUpdateUseCase: TeacherProfileUpdateUseCase

        @Test
        fun `should return forbidden error when user isn't a teacher`() {
            val requestBody = """
                {
                    "firstName": "John",
                    "lastName": "Doe"
                }
            """.trimIndent()

            mvc.perform(
                MockMvcRequestBuilders.patch("/api/teacher/profile/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should successfully update basic profile fields`() {
            val testTeacher = testTeacher()
            val updatedTeacher = mockk<TeacherProfile>(relaxed = true) {
                every { id } returns testTeacher.id
                every { firstName } returns "John"
                every { lastName } returns "Doe"
                every { state } returns "California"
            }
            val requestBody = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "state": "California"
                }
            """.trimIndent()

            every {
                teacherProfileUpdateUseCase.update(testTeacher.id, any())
            } returns updatedTeacher.right()

            mvc.perform(
                MockMvcRequestBuilders.patch("/api/teacher/profile/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.firstName").value("John"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.lastName").value("Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.state").value("California"))

            verify { teacherProfileUpdateUseCase.update(testTeacher.id, any()) }
        }

        @Test
        fun `should successfully update profile with school roles and grades`() {
            val testTeacher = testTeacher()
            val updatedTeacher = mockk<TeacherProfile>(relaxed = true) {
                every { id } returns testTeacher.id
                every { schoolRoles } returns listOf(SchoolRole.TEACHER, SchoolRole.COORDINATOR)
                every { grades } returns listOf(GradeLevel.FIRST_GRADE, GradeLevel.SECOND_GRADE)
            }
            val requestBody = """
                {
                    "schoolRoles": ["TEACHER", "COORDINATOR"],
                    "grades": ["FIRST_GRADE", "SECOND_GRADE"]
                }
            """.trimIndent()

            every {
                teacherProfileUpdateUseCase.update(testTeacher.id, any())
            } returns updatedTeacher.right()

            mvc.perform(
                MockMvcRequestBuilders.patch("/api/teacher/profile/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teacherProfileUpdateUseCase.update(testTeacher.id, any()) }
        }

        @Test
        fun `should successfully update profile with subjects`() {
            val testTeacher = testTeacher()
            val updatedTeacher = mockk<TeacherProfile>(relaxed = true) {
                every { id } returns testTeacher.id
                every { subjects } returns "Mathematics, Computer Science"
            }
            val requestBody = """
                {
                    "subjects": "Mathematics, Computer Science"
                }
            """.trimIndent()

            every {
                teacherProfileUpdateUseCase.update(testTeacher.id, any())
            } returns updatedTeacher.right()

            mvc.perform(
                MockMvcRequestBuilders.patch("/api/teacher/profile/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teacherProfileUpdateUseCase.update(testTeacher.id, any()) }
        }

        @Test
        fun `should successfully update marketing email subscription`() {
            val testTeacher = testTeacher()
            val updatedTeacher = mockk<TeacherProfile>(relaxed = true) {
                every { id } returns testTeacher.id
                every { marketingEmailSubscribed } returns true
            }
            val requestBody = """
                {
                    "marketingEmailSubscribed": true
                }
            """.trimIndent()

            every {
                teacherProfileUpdateUseCase.update(testTeacher.id, any())
            } returns updatedTeacher.right()

            mvc.perform(
                MockMvcRequestBuilders.patch("/api/teacher/profile/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { teacherProfileUpdateUseCase.update(testTeacher.id, any()) }
        }
    }
}
