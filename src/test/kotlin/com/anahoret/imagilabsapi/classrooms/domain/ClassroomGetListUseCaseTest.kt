package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Classroom get list use case")
class ClassroomGetListUseCaseTest {

    private val coTeacherService = mockk<CoTeacherService>()
    private val classroomService = mockk<ClassroomService>()

    private val classroomGetListUseCase = ClassroomGetListUseCaseImpl(classroomService, coTeacherService)

    @Test
    fun `should return list with classrooms where teacher is owner`() {
        val teacherId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()

        val classroom = Classroom(
            classroomId,
            "New classroom",
            "1111",
            5L,
            2L,
            teacherId,
            2L,
            blocked = false
        )

        val emptyListCoClassroomIds = emptyList<UUID>()
        val emptyCoClassrooms = emptyList<Classroom>()

        every { classroomService.listByTeacher(teacherId) } returns listOf(classroom)
        every { coTeacherService.getClassroomIdListByTeacherId(teacherId) } returns emptyListCoClassroomIds
        every { classroomService.getAllClassroomAsCoTeacher(emptyListCoClassroomIds) } returns emptyCoClassrooms

        val result = classroomGetListUseCase.getList(teacherId)

        assertFalse(result.isEmpty())
        assertTrue(result.size == 1)
        assertTrue(result.find { item -> item.teacherRole == TeacherRole.OWNER } != null)
    }

    @Test
    fun `should return list with classrooms where teacher is owner and co-teacher`() {
        val teacherId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()

        val classroom = Classroom(
            classroomId,
            "New classroom",
            "1111",
            5L,
            2L,
            teacherId,
            2L,
            blocked = false
        )

        val coTeacherClassroom = Classroom(
            classroomId,
            "Co teacher classroom",
            "4133",
            8L,
            1L,
            teacherId,
            2L,
            blocked = false,
            teacherRole = TeacherRole.CO_TEACHER
        )

        val emptyListCoClassroomIds = listOf(UUID.randomUUID())

        every { classroomService.listByTeacher(teacherId) } returns listOf(classroom)
        every { coTeacherService.getClassroomIdListByTeacherId(teacherId) } returns emptyListCoClassroomIds
        every { classroomService.getAllClassroomAsCoTeacher(emptyListCoClassroomIds) } returns listOf(coTeacherClassroom)

        val result = classroomGetListUseCase.getList(teacherId)

        assertFalse(result.isEmpty())
        assertTrue(result.size == 2)
        assertTrue(result.find { item -> item.teacherRole == TeacherRole.OWNER } != null)
        assertTrue(result.find { item -> item.teacherRole == TeacherRole.CO_TEACHER } != null)
    }

}
