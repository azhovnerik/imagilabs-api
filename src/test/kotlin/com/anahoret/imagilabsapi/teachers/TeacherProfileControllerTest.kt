package com.anahoret.imagilabsapi.teachers

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.teachers.domain.TeacherDeleteUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherGetStatisticUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherGetUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.storage.TeacherStatistic
import com.anahoret.imagilabsapi.teachers.web.TeacherProfileController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.context.junit.jupiter.SpringExtension
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

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
}
