package com.anahoret.imagilabsapi.projectclassroomshare.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.userclassroomlink.domain.UserClassroomLinkService
import org.springframework.stereotype.Service

interface ProjectClassroomUnshareUseCase {

    fun unshare(
        sharedBy: UserProfile,
        projectClassroomShareChangeRequest: ProjectClassroomShareChangeRequest
    ): Either<OperationError, Unit>
}

@Service
class ProjectClassroomUnshareUseCaseImpl(
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val projectService: ProjectService,
    private val userClassroomLinkService: UserClassroomLinkService,
    private val projectAccessService: ProjectAccessService
) : ProjectClassroomUnshareUseCase {

    override fun unshare(
        sharedBy: UserProfile,
        projectClassroomShareChangeRequest: ProjectClassroomShareChangeRequest
    ): Either<OperationError, Unit> {
        val projectId = projectClassroomShareChangeRequest.projectId
        val classroomIds = projectClassroomShareChangeRequest.classroomIds

        val project = projectService.getProjectById(projectId)
            ?: return NotFoundError("PROJECT_NOT_FOUND").left()

        if (!projectAccessService.canUnshare(sharedBy, project, classroomIds))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()

        if (classroomIds.any { !userClassroomLinkService.isLinkedToClassroom(sharedBy, it) })
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        projectClassroomShareService.unshareFromAll(projectId, classroomIds)
        projectService.updateLastModifiedDate(projectId)
        return Unit.right()
    }

}
