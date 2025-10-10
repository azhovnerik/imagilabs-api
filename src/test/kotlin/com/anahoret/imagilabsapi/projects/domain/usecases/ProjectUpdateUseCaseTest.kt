package com.anahoret.imagilabsapi.projects.domain.usecases

import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomPermissions
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareDetailsListUseCase
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.users.UserType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ProjectUpdateUseCaseTest {

    private val projectService = mockk<ProjectService>()
    private val projectAccessService = mockk<ProjectAccessService>()
    private val projectOwnerGetUseCase = mockk<ProjectOwnerGetUseCase>()
    private val projectClassroomShareDetailsListUseCase = mockk<ProjectClassroomShareDetailsListUseCase>()
    private val classroomService = mockk<ClassroomService>()

    private val useCase = ProjectUpdateUseCaseImpl(
        projectService,
        projectAccessService,
        projectOwnerGetUseCase,
        projectClassroomShareDetailsListUseCase,
        classroomService
    )

    @Test
    fun `update returns subscription required when classroom is blocked`() {
        val teacher = testTeacher()
        val projectId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val project = Project(projectId, "p", teacher.id, UserType.TEACHER, "print(1)", null, 0, 0)

        every { projectService.getProjectById(projectId) } returns project
        every { projectAccessService.canEdit(teacher, project, classroomId) } returns true
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

        val result = useCase.update(classroomId, teacher, projectId, mockk())
        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}

