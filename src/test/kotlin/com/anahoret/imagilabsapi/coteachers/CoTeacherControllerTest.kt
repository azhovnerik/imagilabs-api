package com.anahoret.imagilabsapi.coteachers

import arrow.core.left
import arrow.core.prependTo
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomTeacher
import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.coteachers.domain.*
import com.anahoret.imagilabsapi.coteachers.web.CoTeachersController
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

@DisplayName("Co-teacher controller")
class CoTeacherControllerTest {

    @ExtendWith(SpringExtension::class)
    @DisplayName("when adding bundle to teacher")
    @Nested
    @WebMvcTest(
        CoTeachersController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class GetAllCoTeacherTest : ControllerTest() {
        @MockkBean
        lateinit var invitationCoTeacherUseCase: InvitationCoTeacherUseCase

        @MockkBean
        lateinit var invitationCoTeacherAcceptUseCase: InvitationCoTeacherAcceptUseCase

        @MockkBean
        lateinit var coTeachersByClassroomIdUseCase: CoTeachersClassroomIdUseCase

        @MockkBean
        lateinit var coTeacherRemoveUseCase: CoTeacherRemoveUseCase

        @MockkBean
        lateinit var coTeacherLeaveUseCase: CoTeacherLeaveUseCase

        private val classroomId = UUID.randomUUID()

        @Test
        fun `should return forbidden error`() {
            mvc.perform(
                MockMvcRequestBuilders.get("/api/classrooms/$classroomId/co-teachers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return classroom not found error`() {
            every { coTeachersByClassroomIdUseCase.getAll(classroomId) } returns NotFoundError("").left()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/classrooms/$classroomId/co-teachers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher()
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            every { coTeachersByClassroomIdUseCase.getAll(classroomId) }
        }

        @Test
        fun `should return success when teacher get all co-teachers`() {

            every { coTeachersByClassroomIdUseCase.getAll(classroomId) } returns emptyList<CoTeacher>().right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/classrooms/$classroomId/co-teachers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher()
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            every { coTeachersByClassroomIdUseCase.getAll(classroomId) }
        }

        @Test
        fun `should return success when student get all co-teachers`() {

            every { coTeachersByClassroomIdUseCase.getAll(classroomId) } returns emptyList<CoTeacher>().right()

            mvc.perform(
                MockMvcRequestBuilders.get("/api/classrooms/$classroomId/co-teachers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            every { coTeachersByClassroomIdUseCase.getAll(classroomId) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when adding bundle to teacher")
    @Nested
    @WebMvcTest(
        CoTeachersController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class InviteTeacherTest : ControllerTest() {
        @MockkBean
        lateinit var invitationCoTeacherUseCase: InvitationCoTeacherUseCase

        @MockkBean
        lateinit var invitationCoTeacherAcceptUseCase: InvitationCoTeacherAcceptUseCase

        @MockkBean
        lateinit var coTeachersByClassroomIdUseCase: CoTeachersClassroomIdUseCase

        @MockkBean
        lateinit var coTeacherRemoveUseCase: CoTeacherRemoveUseCase

        @MockkBean
        lateinit var coTeacherLeaveUseCase: CoTeacherLeaveUseCase

        private val classroomId = UUID.randomUUID()
        private val teacherEmail = "teacher@example.com"
        private val payload = JSONObject()
            .put("teacherEmail", teacherEmail)
            .toString()

        @Test
        fun `should return forbidden error when student try invite teacher`() {
            mvc.perform(
                MockMvcRequestBuilders.post("/api/classrooms/$classroomId/invite-teacher")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return bad request when email not valid`() {
            val teacherProfile = testTeacher()
            val request = InvitationCoTeacherRequest(teacherEmail)

            every {
                invitationCoTeacherUseCase.invite(
                    classroomId,
                    request,
                    teacherProfile.id
                )
            } returns ValidationError("").left()

            mvc.perform(
                MockMvcRequestBuilders.post("/api/classrooms/$classroomId/invite-teacher")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)

            verify { invitationCoTeacherUseCase.invite(classroomId, request, teacherProfile.id) }
        }

        @Test
        fun `should return classroom not found error`() {
            val teacherProfile = testTeacher()
            val request = InvitationCoTeacherRequest(teacherEmail)

            every {
                invitationCoTeacherUseCase.invite(
                    classroomId,
                    request,
                    teacherProfile.id
                )
            } returns NotFoundError("").left()

            mvc.perform(
                MockMvcRequestBuilders.post("/api/classrooms/$classroomId/invite-teacher")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            verify { invitationCoTeacherUseCase.invite(classroomId, request, teacherProfile.id) }
        }

        @Test
        fun `should return forbidden when not owner of classroom try to invite teacher`() {
            val teacherProfile = testTeacher()
            val request = InvitationCoTeacherRequest(teacherEmail)

            every {
                invitationCoTeacherUseCase.invite(
                    classroomId,
                    request,
                    teacherProfile.id
                )
            } returns AccessDeniedError("").left()

            mvc.perform(
                MockMvcRequestBuilders.post("/api/classrooms/$classroomId/invite-teacher")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isForbidden)

            verify { invitationCoTeacherUseCase.invite(classroomId, request, teacherProfile.id) }
        }

        @Test
        fun `should return success`() {
            val teacherProfile = testTeacher()
            val request = InvitationCoTeacherRequest(teacherEmail)
            val classroomTeacher = ClassroomTeacher(
                UUID.randomUUID(),
                teacherEmail,
                TeacherRole.CO_TEACHER_PENDING,
                false,
                "Someone",
                UUID.randomUUID()
            )

            every {
                invitationCoTeacherUseCase.invite(
                    classroomId,
                    request,
                    teacherProfile.id
                )
            } returns classroomTeacher.right()

            mvc.perform(
                MockMvcRequestBuilders.post("/api/classrooms/$classroomId/invite-teacher")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { invitationCoTeacherUseCase.invite(classroomId, request, teacherProfile.id) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when co-teacher accept invitation")
    @Nested
    @WebMvcTest(
        CoTeachersController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class AcceptInvitationTest : ControllerTest() {
        @MockkBean
        lateinit var invitationCoTeacherUseCase: InvitationCoTeacherUseCase

        @MockkBean
        lateinit var invitationCoTeacherAcceptUseCase: InvitationCoTeacherAcceptUseCase

        @MockkBean
        lateinit var coTeachersByClassroomIdUseCase: CoTeachersClassroomIdUseCase

        @MockkBean
        lateinit var coTeacherRemoveUseCase: CoTeacherRemoveUseCase

        @MockkBean
        lateinit var coTeacherLeaveUseCase: CoTeacherLeaveUseCase

        private val invitationId = UUID.randomUUID()

        @Test
        fun `should return forbidden error`() {
            mvc.perform(
                MockMvcRequestBuilders.put("/api/classrooms/$invitationId/accept-invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return error`() {
            val teacherProfile = testTeacher()

            every {
                invitationCoTeacherAcceptUseCase.accept(
                    invitationId,
                    teacherProfile
                )
            } returns NotFoundError("").left()

            mvc.perform(
                MockMvcRequestBuilders.put("/api/classrooms/$invitationId/accept-invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            verify { invitationCoTeacherAcceptUseCase.accept(invitationId, teacherProfile) }
        }

        @Test
        fun `should return access denied error`() {
            val teacherProfile = testTeacher()

            every {
                invitationCoTeacherAcceptUseCase.accept(
                    invitationId,
                    teacherProfile
                )
            } returns AccessDeniedError("").left()

            mvc.perform(
                MockMvcRequestBuilders.put("/api/classrooms/$invitationId/accept-invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isForbidden)

            verify { invitationCoTeacherAcceptUseCase.accept(invitationId, teacherProfile) }
        }

        @Test
        fun `should return success`() {
            val teacherProfile = testTeacher()
            val classroom = Classroom(
                UUID.randomUUID(), "", "sdf2", 0, 0,
                UUID.randomUUID(), 1, TeacherRole.OWNER
            )

            every { invitationCoTeacherAcceptUseCase.accept(invitationId, teacherProfile) } returns classroom.right()

            mvc.perform(
                MockMvcRequestBuilders.put("/api/classrooms/$invitationId/accept-invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { invitationCoTeacherAcceptUseCase.accept(invitationId, teacherProfile) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when teacher remove co-teacher from classroom")
    @Nested
    @WebMvcTest(
        CoTeachersController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class DeleteCoTeacherByTeacherTest : ControllerTest() {
        @MockkBean
        lateinit var invitationCoTeacherUseCase: InvitationCoTeacherUseCase

        @MockkBean
        lateinit var invitationCoTeacherAcceptUseCase: InvitationCoTeacherAcceptUseCase

        @MockkBean
        lateinit var coTeachersByClassroomIdUseCase: CoTeachersClassroomIdUseCase

        @MockkBean
        lateinit var coTeacherRemoveUseCase: CoTeacherRemoveUseCase

        @MockkBean
        lateinit var coTeacherLeaveUseCase: CoTeacherLeaveUseCase

        private val coTeacherId = UUID.randomUUID()
        private val classroomId = UUID.randomUUID()

        @Test
        fun `should return forbidden error`() {
            mvc.perform(
                MockMvcRequestBuilders.delete("/api/classrooms/$classroomId/$coTeacherId/remove")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return not found error`() {
            val teacherProfile = testTeacher()

            every {
                coTeacherRemoveUseCase.remove(
                    classroomId,
                    coTeacherId,
                    teacherProfile.id
                )
            } returns NotFoundError("").left()

            mvc.perform(
                MockMvcRequestBuilders.delete("/api/classrooms/$classroomId/$coTeacherId/remove")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            verify { coTeacherRemoveUseCase.remove(classroomId, coTeacherId, teacherProfile.id) }
        }

        @Test
        fun `should return access denied error`() {
            val teacherProfile = testTeacher()

            every {
                coTeacherRemoveUseCase.remove(
                    classroomId,
                    coTeacherId,
                    teacherProfile.id
                )
            } returns AccessDeniedError("").left()

            mvc.perform(
                MockMvcRequestBuilders.delete("/api/classrooms/$classroomId/$coTeacherId/remove")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isForbidden)

            verify { coTeacherRemoveUseCase.remove(classroomId, coTeacherId, teacherProfile.id) }
        }

        @Test
        fun `should return success`() {
            val teacherProfile = testTeacher()

            every { coTeacherRemoveUseCase.remove(classroomId, coTeacherId, teacherProfile.id) } returns Unit.right()

            mvc.perform(
                MockMvcRequestBuilders.delete("/api/classrooms/$classroomId/$coTeacherId/remove")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { coTeacherRemoveUseCase.remove(classroomId, coTeacherId, teacherProfile.id) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when teacher remove co-teacher from classroom")
    @Nested
    @WebMvcTest(
        CoTeachersController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class LeaveCoTeacherFromClassroomTest : ControllerTest() {
        @MockkBean
        lateinit var invitationCoTeacherUseCase: InvitationCoTeacherUseCase

        @MockkBean
        lateinit var invitationCoTeacherAcceptUseCase: InvitationCoTeacherAcceptUseCase

        @MockkBean
        lateinit var coTeachersByClassroomIdUseCase: CoTeachersClassroomIdUseCase

        @MockkBean
        lateinit var coTeacherRemoveUseCase: CoTeacherRemoveUseCase

        @MockkBean
        lateinit var coTeacherLeaveUseCase: CoTeacherLeaveUseCase

        private val classroomId = UUID.randomUUID()

        @Test
        fun `should return forbidden error`() {
            mvc.perform(
                MockMvcRequestBuilders.delete("/api/classrooms/$classroomId/co-teacher/leave")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return not found error`() {
            val teacherProfile = testTeacher()

            every { coTeacherLeaveUseCase.leave(classroomId, teacherProfile.id) } returns NotFoundError("").left()

            mvc.perform(
                MockMvcRequestBuilders.delete("/api/classrooms/$classroomId/co-teacher/leave")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isNotFound)

            verify { coTeacherLeaveUseCase.leave(classroomId, teacherProfile.id) }
        }

        @Test
        fun `should return access denied error`() {
            val teacherProfile = testTeacher()

            every { coTeacherLeaveUseCase.leave(classroomId, teacherProfile.id) } returns AccessDeniedError("").left()

            mvc.perform(
                MockMvcRequestBuilders.delete("/api/classrooms/$classroomId/co-teacher/leave")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().isForbidden)

            verify { coTeacherLeaveUseCase.leave(classroomId, teacherProfile.id) }
        }

        @Test
        fun `should return success`() {
            val teacherProfile = testTeacher()

            every { coTeacherLeaveUseCase.leave(classroomId, teacherProfile.id) } returns Unit.right()

            mvc.perform(
                MockMvcRequestBuilders.delete("/api/classrooms/$classroomId/co-teacher/leave")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(teacherProfile)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { coTeacherLeaveUseCase.leave(classroomId, teacherProfile.id) }
        }
    }
}
