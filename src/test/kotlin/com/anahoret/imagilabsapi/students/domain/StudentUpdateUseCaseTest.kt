package com.anahoret.imagilabsapi.students.domain

import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomPermissions
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testTeacher
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

    private val useCase = StudentUpdateUseCaseImpl(
        studentAccessService,
        studentProfileService,
        studentUpdateRequestValidator,
        classroomService
    )

    @Test
    fun `update returns subscription required when classroom is blocked`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val student = StudentProfile(studentId, "n", "u", 0, classroomId)
        val credentials = StudentClassroomCredentials(UUID.randomUUID(), "u", "ac", "p")

        every { studentProfileService.getStudentById(studentId) } returns student
        every { studentProfileService.getStudentCredentials(studentId) } returns credentials
        every { classroomService.getById(classroomId) } returns Classroom(
            classroomId,
            name = "c",
            accessCode = "ac",
            studentsCount = 0,
            projectsCount = 0,
            teacherId = teacher.id,
            teachersCount = 1,
            blocked = true,
            permissions = ClassroomPermissions(true)
        )

        val result = useCase.update(teacher, studentId, mockk())
        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}
