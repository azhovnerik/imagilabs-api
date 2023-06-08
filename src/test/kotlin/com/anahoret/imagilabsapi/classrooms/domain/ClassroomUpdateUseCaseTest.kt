package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationErrors
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.students.domain.StudentCreateRequest
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Classroom update use case")
class ClassroomUpdateUseCaseTest {

    private val classroomAccessService = mockk<ClassroomAccessService>()
    private val classroomService = mockk<ClassroomService>()
    private val classroomValidator = mockk<ClassroomValidator>()
    private val studentProfileService = mockk<StudentProfileService>()
    private val coTeacherService = mockk<CoTeacherService>()

    private val classroomUpdateUseCase = ClassroomUpdateUseCaseImpl(
        classroomAccessService,
        classroomService,
        classroomValidator,
        studentProfileService,
        coTeacherService
    )

    private val teacherProfile = mockk<TeacherProfile> {
        every { id } returns UUID.randomUUID()
    }
    private val classroomUpdateRequest = mockk<ClassroomUpdateRequest>()
    private val classroomId = UUID.randomUUID()
    private val classroom = mockk<Classroom> {
        every { id } returns classroomId
    }

    @Test
    fun `should update classroom`() {
        every { classroomUpdateRequest.studentCreateRequests } returns emptyList()
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacherProfile, classroom) } returns true
        every { classroomValidator.validate(teacherProfile, classroomId, classroomUpdateRequest) } returns Unit.right()
        every { classroomService.update(classroomId, classroomUpdateRequest) } returns classroom
        every { studentProfileService.createStudents(classroomId, emptyList()) } returns emptyList()
        every { coTeacherService.isLinkedToClassroom(classroomId, teacherProfile.id) } returns false

        classroomUpdateUseCase.update(teacherProfile, classroomId, classroomUpdateRequest)
        verify { classroomService.update(classroomId, classroomUpdateRequest) }
    }

    @Test
    fun `should call create students method`() {
        val studentCreateRequests = listOf<StudentCreateRequest>(mockk())
        every { classroomUpdateRequest.studentCreateRequests } returns studentCreateRequests
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacherProfile, classroom) } returns true
        every { classroomValidator.validate(teacherProfile, classroomId, classroomUpdateRequest) } returns Unit.right()
        every { classroomService.update(classroomId, classroomUpdateRequest) } returns classroom
        every { studentProfileService.createStudents(classroomId, studentCreateRequests) } returns emptyList()
        every { coTeacherService.isLinkedToClassroom(classroomId, teacherProfile.id) } returns false

        classroomUpdateUseCase.update(teacherProfile, classroomId, classroomUpdateRequest)
        verify { studentProfileService.createStudents(classroomId, studentCreateRequests) }
    }

    @Test
    fun `should return error if classroom does not exist`() {
        every { classroomService.getById(classroomId) } returns null
        val result = classroomUpdateUseCase.update(teacherProfile, classroomId, classroomUpdateRequest)
        assertEquals(NotFoundError("CLASSROOM_NOT_FOUND").left(), result)
    }

    @Test
    fun `should return error if teacher has no rights to update the classroom`() {
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacherProfile, classroom) } returns false
        val result = classroomUpdateUseCase.update(teacherProfile, classroomId, classroomUpdateRequest)
        assertEquals(AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left(), result)
    }

    @Test
    fun `should return error if update request is invalid`() {
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacherProfile, classroom) } returns true
        val validationError = listOf(ValidationError.FieldIsBlank("NAME"))
        every {
            classroomValidator.validate(
                teacherProfile,
                classroomId,
                classroomUpdateRequest
            )
        } returns validationError.left()
        val result = classroomUpdateUseCase.update(teacherProfile, classroomId, classroomUpdateRequest)
        assertEquals(ValidationErrors(validationError).left(), result)
    }

    @Test
    fun `should return error if classroom not found on update`() {
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacherProfile, classroom) } returns true
        every { classroomValidator.validate(teacherProfile, classroomId, classroomUpdateRequest) } returns Unit.right()
        every { classroomService.update(classroomId, classroomUpdateRequest) } returns null
        val result = classroomUpdateUseCase.update(teacherProfile, classroomId, classroomUpdateRequest)
        assertEquals(NotFoundError("CLASSROOM_NOT_FOUND").left(), result)
    }

    @Test
    fun `should return updated classroom`() {
        val updatedClassroom = mockk<Classroom> {
            every { id } returns classroomId
        }
        val studentCreateRequests = listOf<StudentCreateRequest>(mockk())
        every { classroomUpdateRequest.studentCreateRequests } returns studentCreateRequests
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacherProfile, classroom) } returns true
        every { classroomValidator.validate(teacherProfile, classroomId, classroomUpdateRequest) } returns Unit.right()
        every { classroomService.update(classroomId, classroomUpdateRequest) } returns updatedClassroom
        every { studentProfileService.createStudents(classroomId, studentCreateRequests) } returns listOf(mockk())
        every { coTeacherService.isLinkedToClassroom(classroomId, teacherProfile.id) } returns false

        val result = classroomUpdateUseCase.update(teacherProfile, classroomId, classroomUpdateRequest)
        assertEquals(updatedClassroom.right(), result)
    }

}
