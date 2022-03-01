package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShare
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service
import java.util.*

interface ListProjectsInClassroomUseCase {

    fun list(listBy: UserProfile, classroomId: UUID): Either<OperationError, List<Project>>
}

@Service
class ListProjectsInClassroomUseCaseImpl(
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val projectService: ProjectService,
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService
) : ListProjectsInClassroomUseCase {

    override fun list(listBy: UserProfile, classroomId: UUID): Either<OperationError, List<Project>> {
        val classroom = classroomService.getById(classroomId) ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canListProjects(listBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_PROJECTS_LIST_DENIED").left()

        return projectClassroomShareService.listByClassroom(classroom.id)
            .map(ProjectClassroomShare::projectId)
            .let { projectService.listByIds(it) }
            .right()
    }
}
