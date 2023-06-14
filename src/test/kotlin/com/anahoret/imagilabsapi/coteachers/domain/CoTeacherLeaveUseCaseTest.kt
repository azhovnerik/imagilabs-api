package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
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
    private val projectService = mockk<ProjectService>()
    private val projectClassroomShareService = mockk<ProjectClassroomShareService>()

    private val coTeacherLeaveUseCase = CoTeacherLeaveUseCaseImpl(
        classroomService, coTeacherService, projectService, projectClassroomShareService
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
        val invitationId = UUID.randomUUID()
        val projectIds = emptyList<UUID>()
        val coTeacherId = UUID.randomUUID()

        every { classroomService.getById(classroomId) } returns mockk<Classroom>()
        every { coTeacherService.getByClassroomIdAndTeacherId(classroomId, currentTeacherId) } returns mockk<CoTeacher> {
            every { id } returns invitationId
            every { teacherId } returns coTeacherId
        }
        every { coTeacherService.deleteCoTeacher(invitationId) } returns Unit
        every { projectService.getAllIdsByOwnerId(coTeacherId) } returns projectIds
        every { projectClassroomShareService.unshareProjectsFromClassroom(projectIds, classroomId) } returns Unit

        val result = coTeacherLeaveUseCase.leave(classroomId, currentTeacherId)

        assertTrue(result.isRight())
    }
}
