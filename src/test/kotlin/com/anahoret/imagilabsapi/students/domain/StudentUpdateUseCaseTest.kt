package com.anahoret.imagilabsapi.students.domain

import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testClassroom
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class StudentUpdateUseCaseTest {

    private val studentAccessService = mockk<StudentAccessService>()
    private val studentProfileService = mockk<StudentProfileService>()
    private val studentUpdateRequestValidator = mockk<StudentUpdateRequestValidator>()
    private val classroomService = mockk<ClassroomService>()
    private val studentClassroomLinkService = mockk<StudentClassroomLinkService>()

    private val useCase = StudentUpdateUseCaseImpl(
        studentAccessService,
        studentProfileService,
        studentUpdateRequestValidator,
        studentClassroomLinkService
    )

    @Test
    fun `update returns subscription required when classroom is blocked`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val student = StudentProfile(studentId, "n", "u", 0)
        val credentials = StudentClassroomCredentials(UUID.randomUUID(), "u", "ac", "p")

        every { studentProfileService.getStudentById(studentId) } returns student
        every { studentProfileService.getStudentCredentials(studentId) } returns credentials
        val classroom = testClassroom(
            id = classroomId,
            teacherId = teacher.id,
            blocked = true
        )
        every { classroomService.getById(classroomId) } returns classroom
        every { studentClassroomLinkService.listClassroomsByStudent(studentId) } returns listOf(classroom)

        val result = useCase.update(teacher, studentId, mockk())
        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}
