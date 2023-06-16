package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.coteachers.domain.*
import com.anahoret.imagilabsapi.signup.domain.ImagiLabsEmailValidator
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Invitation co-teacher use case")
class InvitationCoTeacherUseCaseTest {

    private val emailValidator = mockk<ImagiLabsEmailValidator>()
    private val classroomService = mockk<ClassroomService>()
    private val coTeacherService = mockk<CoTeacherService>()
    private val invitationCoTeacherEmailSender = mockk<InvitationCoTeacherEmailSender>()
    private val teacherProfileService = mockk<TeacherProfileService>()

    private val invitationCoTeacherUseCase = InvitationCoTeacherUseCaseImpl(
        emailValidator,
        classroomService,
        coTeacherService,
        teacherProfileService,
        invitationCoTeacherEmailSender
    )

    @Test
    fun `should return error when email not valid`() {
        val teacherEmail = "teacher@gmail.com"
        every { emailValidator.isValid(teacherEmail) } returns false

        val request = InvitationCoTeacherRequest(teacherEmail)
        val result = invitationCoTeacherUseCase.invite(UUID.randomUUID(), request, testTeacher())

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return error classroom not found`() {
        val teacherEmail = "teacher@gmail.com"
        val classroomId = UUID.randomUUID()
        every { emailValidator.isValid(teacherEmail) } returns true
        every { classroomService.getById(classroomId) } returns null

        val request = InvitationCoTeacherRequest(teacherEmail)
        val result = invitationCoTeacherUseCase.invite(classroomId, request, testTeacher())

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return error when not owner try to invite co-teacher`() {
        val teacherEmail = "teacher@gmail.com"
        val classroomId = UUID.randomUUID()
        val classroom = mockk<Classroom>() {
            every { teacherId } returns UUID.randomUUID()
        }

        every { emailValidator.isValid(teacherEmail) } returns true
        every { classroomService.getById(classroomId) } returns classroom

        val request = InvitationCoTeacherRequest(teacherEmail)
        val result = invitationCoTeacherUseCase.invite(classroomId, request, testTeacher())

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return validation error when co-teachers limit exceeded`() {
        val testTeacher = testTeacher()
        val teacherEmail = testTeacher.email
        val teacherIdInviteTo = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val classroom = mockk<Classroom> {
            every { teacherId } returns testTeacher.id
        }

        every { emailValidator.isValid(teacherEmail) } returns true
        every { classroomService.getById(classroomId) } returns classroom
        every { teacherProfileService.getTeacherIdByEmail(teacherEmail) } returns teacherIdInviteTo
        every { coTeacherService.getCoTeacherCountByClassroomId(classroomId) } returns 5

        val request = InvitationCoTeacherRequest(teacherEmail)
        val result = invitationCoTeacherUseCase.invite(classroomId, request, testTeacher)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return validation error`() {
        val currentTeacher = testTeacher()
        val teacherEmail = currentTeacher.email
        val teacherIdInviteTo = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val classroom = mockk<Classroom> {
            every { teacherId } returns currentTeacher.id
        }

        every { emailValidator.isValid(teacherEmail) } returns true
        every { classroomService.getById(classroomId) } returns classroom
        every { teacherProfileService.getTeacherIdByEmail(teacherEmail) } returns teacherIdInviteTo
        every { coTeacherService.getCoTeacherCountByClassroomId(classroomId) } returns 3

        val request = InvitationCoTeacherRequest(teacherEmail)
        val result = invitationCoTeacherUseCase.invite(classroomId, request, currentTeacher)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return co-teacher`() {
        val invitationEmailTo = "teacher@gmail.com"
        val classroomId = UUID.randomUUID()
        val request = InvitationCoTeacherRequest(invitationEmailTo)
        val currentTeacher = testTeacher()
        val ownerClassroomId = currentTeacher.id
        val teacherIdInviteTo = UUID.randomUUID()

        val testTeacher = testTeacher()
        val classroom = mockk<Classroom> {
            every { teacherId } returns  ownerClassroomId
            every { teacherId } returns testTeacher.id
        }

        val coTeacher = CoTeacher(
            UUID.randomUUID(),
            classroomId,
            invitationEmailTo,
            teacherIdInviteTo,
            TeacherRole.CO_TEACHER_PENDING,
            "Test teacher"
        )

        val preferences = InvitationEmailPreferences(
            fromName = currentTeacher.fullName,
            from = currentTeacher.email,
            sendTo = coTeacher.teacherEmail,
            invitationId = coTeacher.id
        )

        every { emailValidator.isValid(invitationEmailTo) } returns true
        every { classroomService.getById(classroomId) } returns classroom
        every { teacherProfileService.getTeacherIdByEmail(invitationEmailTo) } returns teacherIdInviteTo
        every { coTeacherService.getCoTeacherCountByClassroomId(classroomId) } returns 3
        every { coTeacherService.createCoTeacher(classroomId, invitationEmailTo, teacherIdInviteTo) } returns coTeacher
        every { coTeacherService.isExistsPendingInvite(classroomId, invitationEmailTo) } returns false
        every { invitationCoTeacherEmailSender.send(preferences) } returns Unit

        val result = invitationCoTeacherUseCase.invite(classroomId, request, testTeacher)

        assertTrue(result.isRight())
    }
}
