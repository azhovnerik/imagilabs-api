package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Invitation co-teacher resent use case")
class InvitationCoTeacherResendUseCaseTest {

    private val coTeacherService = mockk<CoTeacherService>()
    private val classroomService = mockk<ClassroomService>()
    private val invitationCoTeacherEmailSender = mockk<InvitationCoTeacherEmailSender>()

    private val invitationId = UUID.randomUUID()
    private val currentTeacherId = UUID.randomUUID()

    private val invitationCoTeacherResendUseCase = InvitationCoTeacherResendUseCaseImpl(
        coTeacherService, classroomService, invitationCoTeacherEmailSender
    )

    @Test
    fun `should return invitation not found`() {
        every { coTeacherService.getCoTeacher(invitationId) } returns null

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return invitation already accepted`() {
        val coTeacher = mockk<CoTeacher> {
            every { teacherId } returns UUID.randomUUID()
            every { coTeacherStatus } returns TeacherRole.CO_TEACHER
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return classroom not found`() {
        val coTeacher = mockk<CoTeacher> {
            every { teacherId } returns null
            every { classroomId } returns UUID.randomUUID()
            every { coTeacherStatus } returns TeacherRole.CO_TEACHER_PENDING
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher
        every { classroomService.getById(coTeacher.classroomId) } returns null

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return that teacher should be owner` () {
        val coTeacher = mockk<CoTeacher> {
            every { teacherId } returns null
            every { classroomId } returns UUID.randomUUID()
            every { coTeacherStatus } returns TeacherRole.CO_TEACHER_PENDING
        }

        val classroom = mockk<Classroom> {
            every { teacherId } returns UUID.randomUUID()
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher
        every { classroomService.getById(coTeacher.classroomId) } returns classroom

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return unit`() {
        val coTeacher = mockk<CoTeacher> {
            every { teacherId } returns null
            every { teacherEmail } returns ""
            every { classroomId } returns UUID.randomUUID()
            every { coTeacherStatus } returns TeacherRole.CO_TEACHER_PENDING
        }

        val classroom = mockk<Classroom> {
            every { teacherId } returns currentTeacherId
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher
        every { classroomService.getById(coTeacher.classroomId) } returns classroom
        every { invitationCoTeacherEmailSender.send(coTeacher.teacherEmail, invitationId) } returns Unit

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacherId)

        assertTrue(result.isRight())
    }
}
