package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.testClassroom
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Invitation co teacher accept use case")
class InvitationCoTeacherAcceptUseCaseTest {

    private val coTeacherService = mockk<CoTeacherService>()
    private val classroomService = mockk<ClassroomService>()
    private val invitationCoTeacherAcceptUseCase = InvitationCoTeacherAcceptUseCaseImpl(coTeacherService, classroomService)

    @Test
    fun `should return not found invitation error`() {
        val invitationId = UUID.randomUUID()
        val teacherProfile = mockk<TeacherProfile>()
        every { coTeacherService.getCoTeacher(invitationId) } returns null

        val result = invitationCoTeacherAcceptUseCase.accept(invitationId, teacherProfile)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return error only invited teacher can accept invitation`() {
        val invitationId = UUID.randomUUID()
        val coTeacher = mockk<CoTeacher> {
            every { teacherEmail } returns "teacher2@gmail.com"
        }
        val currentTeacherProfile = mockk<TeacherProfile> {
            every { email } returns "teacher1@gmail.com"
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher

        val result = invitationCoTeacherAcceptUseCase.accept(invitationId, currentTeacherProfile)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return classroom not found error`() {
        val invitedTeacherEmail = "teacher@gmail.com"
        val invitationId = UUID.randomUUID()
        val coTeacher = mockk<CoTeacher> {
            every { teacherEmail } returns invitedTeacherEmail
            every { classroomId } returns UUID.randomUUID()
        }
        val currentTeacherProfile = mockk<TeacherProfile> {
            every { email } returns invitedTeacherEmail
        }

        every { coTeacherService.getCoTeacher(invitationId) } returns coTeacher
        every { classroomService.getById(coTeacher.classroomId) } returns null

        val result = invitationCoTeacherAcceptUseCase.accept(invitationId, currentTeacherProfile)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return unit when teacherId is defined`() {
        val coTeacherId = UUID.randomUUID()
        val coTeacherClassroomId = UUID.randomUUID()
        val teacherProfile = testTeacher()
        val coTeacher = mockk<CoTeacher> {
            every { teacherEmail } returns teacherProfile.email
            every { classroomId } returns coTeacherClassroomId
            every { teacherId } returns teacherProfile.id
        }

        val classroom = testClassroom()

        every { coTeacherService.getCoTeacher(coTeacherId) } returns coTeacher
        every { classroomService.getById(coTeacherClassroomId) } returns classroom
        every { coTeacherService.acceptInvitation(coTeacherId) } returns Unit

        val result = invitationCoTeacherAcceptUseCase.accept(coTeacherId, teacherProfile)

        assertTrue(result.isRight())
    }

    @Test
    fun `should return unit when teacherId is undefined`() {
        val coTeacherId = UUID.randomUUID()
        val coTeacherClassroomId = UUID.randomUUID()
        val teacherProfile = testTeacher()
        val coTeacher = mockk<CoTeacher> {
            every { teacherEmail } returns teacherProfile.email
            every { classroomId } returns coTeacherClassroomId
            every { teacherId } returns null
        }

        val classroom = testClassroom()

        every { coTeacherService.getCoTeacher(coTeacherId) } returns coTeacher
        every { classroomService.getById(coTeacherClassroomId) } returns classroom
        every { coTeacherService.acceptInvitation(coTeacherId, teacherProfile.id) } returns Unit

        val result = invitationCoTeacherAcceptUseCase.accept(coTeacherId, teacherProfile)

        assertTrue(result.isRight())
    }

}
