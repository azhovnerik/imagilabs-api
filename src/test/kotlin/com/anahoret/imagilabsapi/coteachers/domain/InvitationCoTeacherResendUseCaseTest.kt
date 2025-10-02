package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testTeacher
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
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
    private val currentTeacher = testTeacher()

    private val invitationCoTeacherResendUseCase = InvitationCoTeacherResendUseCaseImpl(
        coTeacherService, classroomService, invitationCoTeacherEmailSender
    )

    @Test
    fun `should return invitation not found`() {
        every { coTeacherService.getCoTeacher(invitationId) } returns null

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacher)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return invitation already accepted`() {
        val coTeacher = mockk<CoTeacher> {
            every { teacherId } returns UUID.randomUUID()
            every { coTeacherStatus } returns TeacherRole.CO_TEACHER
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacher)

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

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacher)

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
            every { blocked } returns false
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher
        every { classroomService.getById(coTeacher.classroomId) } returns classroom

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacher)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return unit`() {
        val coTeacher = mockk<CoTeacher> {
            every { id } returns UUID.randomUUID()
            every { teacherId } returns null
            every { teacherEmail } returns ""
            every { classroomId } returns UUID.randomUUID()
            every { coTeacherStatus } returns TeacherRole.CO_TEACHER_PENDING
        }

        val classroom = mockk<Classroom> {
            every { teacherId } returns currentTeacher.id
            every { blocked } returns false
        }

        val preferences = InvitationEmailPreferences(
            fromName = currentTeacher.fullName,
            from = currentTeacher.email,
            sendTo = coTeacher.teacherEmail,
            invitationId = coTeacher.id
        )

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher
        every { classroomService.getById(coTeacher.classroomId) } returns classroom
        every { invitationCoTeacherEmailSender.send(preferences) } returns Unit

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacher)

        assertTrue(result.isRight())
    }

    @Test
    fun `should return subscription required when classroom is blocked`() {
        val coTeacher = mockk<CoTeacher> {
            every { teacherId } returns null
            every { classroomId } returns UUID.randomUUID()
            every { coTeacherStatus } returns TeacherRole.CO_TEACHER_PENDING
        }

        val classroom = mockk<Classroom> {
            every { teacherId } returns currentTeacher.id
            every { blocked } returns true
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher
        every { classroomService.getById(coTeacher.classroomId) } returns classroom

        val result = invitationCoTeacherResendUseCase.resend(invitationId, currentTeacher)

        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}
