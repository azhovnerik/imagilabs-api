package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.left
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ClassroomGetUseCaseTest {

    private val classroomAccessService = mockk<ClassroomAccessService>()
    private val classroomService = mockk<ClassroomService>()
    private val coTeacherService = mockk<CoTeacherService>()

    private val useCase = ClassroomGetUseCaseImpl(
        classroomAccessService,
        classroomService,
        coTeacherService
    )

    @Test
    fun `get returns forbidden when classroom is blocked for teacher`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(classroomId, "c", "ac", 0, 0, teacher.id, 1, blocked = true)
        every { classroomService.getById(classroomId) } returns classroom

        val result = useCase.get(teacher, classroomId)
        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }

    @Test
    fun `get returns forbidden when classroom is blocked for student`() {
        val classroomId = UUID.randomUUID()
        val student = testStudent(classroomId)
        val classroom = Classroom(classroomId, "c", "ac", 0, 0, UUID.randomUUID(), 1, blocked = true)
        every { classroomService.getById(classroomId) } returns classroom

        val result = useCase.get(student, classroomId)
        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}

