package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacher
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherServiceImpl
import com.anahoret.imagilabsapi.coteachers.domain.CoTeachersClassroomIdUseCaseImpl
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherEntity
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Co-teachers by classroom id use case")
class CoTeachersByClassroomIdUseCaseTest {

    private val classroomService = mockk<ClassroomService>()
    private val coTeacherRepository = mockk<CoTeacherRepository>()
    private val coTeacherService = CoTeacherServiceImpl(coTeacherRepository)

    private val coTeachersByClassroomIdUseCase = CoTeachersClassroomIdUseCaseImpl(
        classroomService, coTeacherService
    )

    @Test
    fun `should return error classroom not found`() {
        val classroomId = UUID.randomUUID()
        every { classroomService.getById(classroomId) } returns null

        val result = coTeachersByClassroomIdUseCase.getAll(classroomId)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return list of co-teachers`() {
        val classroomId = UUID.randomUUID()
        val coTeachers = listOf<CoTeacherEntity>()

        every { classroomService.getById(classroomId) } returns mockk<Classroom>()
        every { coTeacherRepository.findAllByClassroomId(classroomId) } returns coTeachers

        val result = coTeachersByClassroomIdUseCase.getAll(classroomId)

        assertTrue(result.isRight())
    }
}
