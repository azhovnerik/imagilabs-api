package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Co-teacher leave use case")
class CoTeacherLeaveUseCaseTest {

    private val classroomService = mockk<ClassroomService>()
    private val coTeacherService = mockk<CoTeacherService>()

    private val coTeacherLeaveUseCase = CoTeacherLeaveUseCaseImpl(
        classroomService, coTeacherService
    )

    @Test
    fun `should return classroom not found error`() {
        val classroomId = UUID.randomUUID()
        val currentTeacherId = UUID.randomUUID()

        every { classroomService.getById(classroomId) } returns null

        val result = coTeacherLeaveUseCase.leave(classroomId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return co-teacher must be member of classroom error`() {
        val classroomId = UUID.randomUUID()
        val currentTeacherId = UUID.randomUUID()

        every { classroomService.getById(classroomId) } returns mockk<Classroom>()
        every { coTeacherService.getByClassroomIdAndTeacherId(classroomId, currentTeacherId) } returns null

        val result = coTeacherLeaveUseCase.leave(classroomId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `co-teacher should be leaved`() {
        val classroomId = UUID.randomUUID()
        val currentTeacherId = UUID.randomUUID()
        val coTeacherId = UUID.randomUUID()

        every { classroomService.getById(classroomId) } returns mockk<Classroom>()
        every { coTeacherService.getByClassroomIdAndTeacherId(classroomId, currentTeacherId) } returns mockk<CoTeacher> {
            every { id } returns coTeacherId
        }
        every { coTeacherService.deleteCoTeacher(coTeacherId) } returns Unit

        val result = coTeacherLeaveUseCase.leave(classroomId, currentTeacherId)

        assertTrue(result.isRight())
    }
}
