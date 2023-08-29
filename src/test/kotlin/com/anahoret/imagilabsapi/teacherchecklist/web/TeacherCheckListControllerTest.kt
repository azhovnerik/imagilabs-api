package com.anahoret.imagilabsapi.teacherchecklist.web

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.common.testTeacherCheckList
import com.anahoret.imagilabsapi.teacherchecklist.domain.*
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.SHARE_STUDENT_ACCESS_CODE
import com.anahoret.imagilabsapi.teacherchecklist.web.TeacherCheckListController.Companion.TEACHER_CHECK_LIST_COMPLETED_PATH
import com.anahoret.imagilabsapi.teacherchecklist.web.TeacherCheckListController.Companion.TEACHER_CHECK_LIST_COMPLETE_STEP_PATH
import com.anahoret.imagilabsapi.teacherchecklist.web.TeacherCheckListController.Companion.TEACHER_CHECK_LIST_PATH
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
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

@DisplayName("Teacher checklist controller")
class TeacherCheckListControllerTest {

    @ExtendWith(SpringExtension::class)
    @DisplayName("Get teacher checklist")
    @Nested
    @WebMvcTest(
        TeacherCheckListController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class GetTeacherCheckList : ControllerTest() {
        @MockkBean
        lateinit var getTeacherCheckListUseCase: GetTeacherCheckListUseCase

        @MockkBean
        lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

        @MockkBean
        lateinit var completeTeacherCheckListUseCase: CompleteTeacherCheckListUseCase

        @MockkBean
        lateinit var teacherCheckListService: TeacherCheckListService

        @MockkBean
        lateinit var completeTeacherCheckListStepUseCase: CompleteTeacherCheckListStepUseCase

        @MockkBean
        lateinit var updateTeacherCheckListStepUseCase: UpdateTeacherCheckListStepUseCase

        @Test
        fun `should return forbidden error if user is admin`() {
            mvc.perform(
                MockMvcRequestBuilders.get(TEACHER_CHECK_LIST_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return forbidden error if user is student`() {
            mvc.perform(
                MockMvcRequestBuilders.get(TEACHER_CHECK_LIST_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return success`() {
            val testTeacher = testTeacher()
            val teacherCheckList = TeacherCheckList(emptyList())

            every { getTeacherCheckListUseCase.get(testTeacher.id) } returns teacherCheckList.right()

            mvc.perform(
                MockMvcRequestBuilders.get(TEACHER_CHECK_LIST_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { getTeacherCheckListUseCase.get(testTeacher.id) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("Complete teacher checklist step")
    @Nested
    @WebMvcTest(
        TeacherCheckListController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class CompleteTeacherCheckListStep : ControllerTest() {

        @MockkBean
        lateinit var getTeacherCheckListUseCase: GetTeacherCheckListUseCase

        @MockkBean
        lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

        @MockkBean
        lateinit var completeTeacherCheckListUseCase: CompleteTeacherCheckListUseCase

        @MockkBean
        lateinit var teacherCheckListService: TeacherCheckListService

        @MockkBean
        lateinit var completeTeacherCheckListStepUseCase: CompleteTeacherCheckListStepUseCase

        @MockkBean
        lateinit var updateTeacherCheckListStepUseCase: UpdateTeacherCheckListStepUseCase

        private val payload = JSONObject()
            .put("step", "SHARE_STUDENT_ACCESS_CODE")
            .toString()

        @Test
        fun `should return forbidden error when user is admin`() {
            mvc.perform(
                MockMvcRequestBuilders.put(TEACHER_CHECK_LIST_COMPLETE_STEP_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return forbidden error when user is student`() {
            mvc.perform(
                MockMvcRequestBuilders.put(TEACHER_CHECK_LIST_COMPLETE_STEP_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return success`() {
            val testTeacher = testTeacher()
            val teacherCheckList = TeacherCheckList(emptyList())

            every {
                completeTeacherCheckListStepUseCase.complete(
                    testTeacher.id,
                    SHARE_STUDENT_ACCESS_CODE
                )
            } returns teacherCheckList

            mvc.perform(
                MockMvcRequestBuilders.put(TEACHER_CHECK_LIST_COMPLETE_STEP_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { completeTeacherCheckListStepUseCase.complete(testTeacher.id, SHARE_STUDENT_ACCESS_CODE) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("Complete teacher checklist")
    @Nested
    @WebMvcTest(
        TeacherCheckListController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class CompleteTeacherCheckList : ControllerTest() {

        @MockkBean
        lateinit var getTeacherCheckListUseCase: GetTeacherCheckListUseCase

        @MockkBean
        lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

        @MockkBean
        lateinit var completeTeacherCheckListUseCase: CompleteTeacherCheckListUseCase

        @MockkBean
        lateinit var teacherCheckListService: TeacherCheckListService

        @MockkBean
        lateinit var completeTeacherCheckListStepUseCase: CompleteTeacherCheckListStepUseCase

        @MockkBean
        lateinit var updateTeacherCheckListStepUseCase: UpdateTeacherCheckListStepUseCase

        @Test
        fun `should return forbidden error when user is admin`() {
            mvc.perform(
                MockMvcRequestBuilders.post(TEACHER_CHECK_LIST_COMPLETED_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return forbidden error when user is student`() {
            mvc.perform(
                MockMvcRequestBuilders.post(TEACHER_CHECK_LIST_COMPLETED_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return bad request error when check list could not be completed`() {
            val testTeacher = testTeacher()

            every { completeTeacherCheckListUseCase.completeTeacherCheckList(testTeacher) } returns ValidationError("").left()

            mvc.perform(
                MockMvcRequestBuilders.post(TEACHER_CHECK_LIST_COMPLETED_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun `should return success`() {
            val testTeacher = testTeacher()

            every { completeTeacherCheckListUseCase.completeTeacherCheckList(testTeacher) } returns Unit.right()

            mvc.perform(
                MockMvcRequestBuilders.post(TEACHER_CHECK_LIST_COMPLETED_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { completeTeacherCheckListUseCase.completeTeacherCheckList(testTeacher) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("Create teacher checklists")
    @Nested
    @WebMvcTest(
        TeacherCheckListController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class CreateTeacherCheckLists : ControllerTest() {

        @MockkBean
        lateinit var getTeacherCheckListUseCase: GetTeacherCheckListUseCase

        @MockkBean
        lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

        @MockkBean
        lateinit var completeTeacherCheckListUseCase: CompleteTeacherCheckListUseCase

        @MockkBean
        lateinit var teacherCheckListService: TeacherCheckListService

        @MockkBean
        lateinit var completeTeacherCheckListStepUseCase: CompleteTeacherCheckListStepUseCase

        @MockkBean
        lateinit var updateTeacherCheckListStepUseCase: UpdateTeacherCheckListStepUseCase

        @Test
        fun `should return forbidden error when user is student`() {
            mvc.perform(
                MockMvcRequestBuilders.post(TEACHER_CHECK_LIST_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return forbidden error when user is teacher`() {
            mvc.perform(
                MockMvcRequestBuilders.post(TEACHER_CHECK_LIST_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should update teacher check lists`() {
            val teacherId = UUID.randomUUID()
            val teacherCheckList = testTeacherCheckList()
            every { teacherProfileEntityRepository.findAll() } returns listOf(mockk {
                every { id } returns teacherId
            })
            every { teacherCheckListService.createCheckList(teacherId) } returns teacherCheckList
            every { updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList) } returns mockk()
            mvc.perform(
                MockMvcRequestBuilders.post(TEACHER_CHECK_LIST_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isOk)
            verify(exactly = 1) { updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList) }
        }
    }
}
