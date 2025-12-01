package com.anahoret.imagilabsapi.projectclassroomshare.domain

import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testClassroom
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.pythoncompiler.domain.CodeRunUseCase
import com.anahoret.imagilabsapi.userclassroomlink.domain.CoTeacherClassroomLinkService
import com.anahoret.imagilabsapi.userclassroomlink.domain.UserClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ProjectClassroomShareUseCaseTest {

    private val projectClassroomShareService = mockk<ProjectClassroomShareService>(relaxed = true)
    private val projectService = mockk<ProjectService>()
    private val userClassroomLinkService = mockk<UserClassroomLinkService>()
    private val coTeacherClassroomLinkService = mockk<CoTeacherClassroomLinkService>()
    private val projectAccessService = mockk<ProjectAccessService>()
    private val codeRunUseCase = mockk<CodeRunUseCase>()
    private val classroomService = mockk<ClassroomService>()

    private val useCase = ProjectClassroomShareUseCaseImpl(
        projectClassroomShareService,
        projectService,
        userClassroomLinkService,
        coTeacherClassroomLinkService,
        projectAccessService,
        codeRunUseCase,
        classroomService
    )

    @Test
    fun `share returns forbidden when any classroom is blocked`() {
        val teacher = testTeacher()
        val projectId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val changeRequest = ProjectClassroomShareChangeRequest(projectId, listOf(classroomId))

        val project = Project(projectId, "p", teacher.id, UserType.TEACHER, "print(1)", null, 0, 0)
        every { projectService.getProjectById(projectId) } returns project
        every { projectAccessService.canShare(teacher, project, listOf(classroomId)) } returns true
        every { userClassroomLinkService.isLinkedToClassroom(teacher, classroomId) } returns true
        every { coTeacherClassroomLinkService.hasAccessToClassroom(classroomId, teacher) } returns false

        val blockedClassroom = testClassroom(
            id = classroomId,
            teacherId = teacher.id,
            blocked = true
        )
        every { classroomService.listByIds(listOf(classroomId)) } returns listOf(blockedClassroom)

        val result = useCase.share(teacher, changeRequest)
        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}
