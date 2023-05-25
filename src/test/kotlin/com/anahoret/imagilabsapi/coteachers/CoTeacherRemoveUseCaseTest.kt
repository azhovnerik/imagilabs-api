package com.anahoret.imagilabsapi.coteachers

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherRemoveUseCaseImpl
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("Co-teacher remove use case")
class CoTeacherRemoveUseCaseTest {

    private val classroomService = mockk<ClassroomService>()
    private val coTeacherService = mockk<CoTeacherService>()

    private val coTeacherRemoveUseCase = CoTeacherRemoveUseCaseImpl(
        classroomService, coTeacherService
    )

    @Test
    fun `should return classroom not found error`() {
        val classroomId = UUID.randomUUID()
        val coTeacherId = UUID.randomUUID()
        val currentTeacherId = UUID.randomUUID()
        every { classroomService.getById(classroomId) } returns null

        val result = coTeacherRemoveUseCase.remove(classroomId, coTeacherId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return co-teacher not found error`() {
        val classroomId = UUID.randomUUID()
        val coTeacherId = UUID.randomUUID()
        val currentTeacherId = UUID.randomUUID()

        every { classroomService.getById(classroomId) } returns mockk<Classroom>()
        every { coTeacherService.existsById(coTeacherId) } returns false

        val result = coTeacherRemoveUseCase.remove(classroomId, coTeacherId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return access denied error`() {
        val classroomId = UUID.randomUUID()
        val coTeacherId = UUID.randomUUID()
        val currentTeacherId = UUID.randomUUID()

        every { classroomService.getById(classroomId) } returns mockk<Classroom> {
            every { teacherId } returns UUID.randomUUID()
        }
        every { coTeacherService.existsById(coTeacherId) } returns false

        val result = coTeacherRemoveUseCase.remove(classroomId, coTeacherId, currentTeacherId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return unit`() {
        val classroomId = UUID.randomUUID()
        val coTeacherId = UUID.randomUUID()
        val currentTeacherId = UUID.randomUUID()

        every { classroomService.getById(classroomId) } returns mockk<Classroom> {
            every { teacherId } returns currentTeacherId
        }
        every { coTeacherService.existsById(coTeacherId) } returns true
        every { coTeacherService.deleteCoTeacher(coTeacherId) } returns Unit

        val result = coTeacherRemoveUseCase.remove(classroomId, coTeacherId, currentTeacherId)

        assertTrue(result.isRight())
    }

}
