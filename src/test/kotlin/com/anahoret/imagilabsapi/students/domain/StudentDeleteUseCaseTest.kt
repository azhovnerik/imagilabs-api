package com.anahoret.imagilabsapi.students.domain

import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testClassroom
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class StudentDeleteUseCaseTest {

    private val studentAccessService = mockk<StudentAccessService>()
    private val studentProfileService = mockk<StudentProfileService>()
    private val projectService = mockk<ProjectService>()
    private val projectClassroomShareService = mockk<ProjectClassroomShareService>()
    private val classroomService = mockk<ClassroomService>()
    private val studentClassroomLinkService = mockk<StudentClassroomLinkService>()

    private val useCase = StudentDeleteUseCaseImpl(
        studentAccessService,
        studentProfileService,
        projectService,
        projectClassroomShareService,
        studentClassroomLinkService
    )

    @Test
    fun `delete returns subscription required when classroom is blocked`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val student = StudentProfile(studentId, "n", "u", 0)

        every { studentProfileService.getStudentById(studentId) } returns student
        every { classroomService.getById(classroomId) } returns testClassroom(
            id = classroomId,
            teacherId = teacher.id,
            blocked = true
        )
        every { studentClassroomLinkService.listClassroomsByStudent(studentId) } returns listOf(
            mockk {
                every { blocked } returns true
            }
        )

        val result = useCase.delete(teacher, studentId)
        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}

