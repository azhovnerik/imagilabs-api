package com.anahoret.imagilabsapi.coteachers

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.coteachers.domain.*
import com.anahoret.imagilabsapi.signup.domain.ImagiLabsEmailValidator
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

    private val invitationCoTeacherUseCase = InvitationCoTeacherUseCaseImpl(
        emailValidator,
        classroomService,
        coTeacherService,
        invitationCoTeacherEmailSender
    )

    @Test
    fun `should return error when email not valid`() {
        val teacherEmail = "teacher@gmail.com"
        every { emailValidator.isValid(teacherEmail) } returns false

        val request = InvitationCoTeacherRequest(teacherEmail)
        val result = invitationCoTeacherUseCase.invite(UUID.randomUUID(), request, UUID.randomUUID())

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return error classroom not found`() {
        val teacherEmail = "teacher@gmail.com"
        val classroomId = UUID.randomUUID()
        every { emailValidator.isValid(teacherEmail) } returns true
        every { classroomService.getById(classroomId) } returns null

        val request = InvitationCoTeacherRequest(teacherEmail)
        val result = invitationCoTeacherUseCase.invite(classroomId, request, UUID.randomUUID())

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
        val result = invitationCoTeacherUseCase.invite(classroomId, request, UUID.randomUUID())

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return Unit`() {
        val teacherEmail = "teacher@gmail.com"
        val currentTeacherId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val classroom = mockk<Classroom> {
            every { teacherId } returns currentTeacherId
        }

        every { emailValidator.isValid(teacherEmail) } returns true
        every { classroomService.getById(classroomId) } returns classroom
        every { coTeacherService.createCoTeacher(classroomId, teacherEmail) } returns mockk<CoTeacher>()
        every { invitationCoTeacherEmailSender.send(teacherEmail, classroomId) } returns Unit

        val request = InvitationCoTeacherRequest(teacherEmail)
        val result = invitationCoTeacherUseCase.invite(classroomId, request, currentTeacherId)

        assertTrue(result.isRight())
    }
}
