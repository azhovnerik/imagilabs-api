package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.students.domain.StudentCreateRequest
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teacherchecklist.domain.CompleteTeacherCheckListStepUseCase
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Classroom create use case")
class ClassroomCreateUseCaseTest {

    private val classroomValidator = mockk<ClassroomValidator>()
    private val classroomService = mockk<ClassroomService>()
    private val studentProfileService = mockk<StudentProfileService>()
    private val teacherSubscriptionService = mockk<TeacherSubscriptionService>()
    private val completeTeacherChecklistStepUseCase = mockk<CompleteTeacherCheckListStepUseCase>()

    private val classroomCreateUseCase = ClassroomCreateUseCaseImpl(
        classroomValidator,
        classroomService,
        studentProfileService,
        teacherSubscriptionService,
        completeTeacherChecklistStepUseCase
    )

    @Test
    fun `should return error when classroom limit exceeded`() {
        val teacherProfile = mockk<TeacherProfile> {
            every { id } returns UUID.randomUUID()
        }
        val classroomCreateRequest = mockk<ClassroomCreateRequest>()

        every { teacherSubscriptionService.canCreateClassroom(teacherProfile) } returns false
        every { classroomValidator.validate(teacherProfile, classroomCreateRequest) } returns Unit.right()
        val result = classroomCreateUseCase.create(teacherProfile, classroomCreateRequest)
        assertTrue(result.isLeft())
    }

    @Test
    fun `should return error when create request is invalid`() {
        val teacherProfile = mockk<TeacherProfile> {
            every { id } returns UUID.randomUUID()
        }
        val classroomCreateRequest = mockk<ClassroomCreateRequest>()

        every { teacherSubscriptionService.canCreateClassroom(teacherProfile) } returns true
        every { classroomValidator.validate(teacherProfile, classroomCreateRequest) } returns mockk<List<ValidationError>>().left()
        val result = classroomCreateUseCase.create(teacherProfile, classroomCreateRequest)
        assertTrue(result.isLeft())
    }

    @Test
    fun `should return created class`() {
        val teacherProfile = mockk<TeacherProfile> {
            every { id } returns UUID.randomUUID()
        }
        val studentCreateRequests = listOf(StudentCreateRequest("Aria Stark"))
        val classroomCreateRequest = mockk<ClassroomCreateRequest> {
            every { this@mockk.studentCreateRequests } returns studentCreateRequests
        }
        val classroom = mockk<Classroom> {
            every { id } returns UUID.randomUUID()
        }

        every { studentProfileService.createStudents(classroom.id, studentCreateRequests) } returns listOf(mockk())
        every { completeTeacherChecklistStepUseCase.complete(teacherProfile.id, CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM) } returns Unit
        every { teacherSubscriptionService.canCreateClassroom(teacherProfile) } returns true
        every { classroomValidator.validate(teacherProfile, classroomCreateRequest) } returns Unit.right()
        every { classroomService.create(teacherProfile.id, classroomCreateRequest) } returns classroom

        val result = classroomCreateUseCase.create(teacherProfile, classroomCreateRequest)
        assertEquals(classroom, result.getOrNull())
    }

}
