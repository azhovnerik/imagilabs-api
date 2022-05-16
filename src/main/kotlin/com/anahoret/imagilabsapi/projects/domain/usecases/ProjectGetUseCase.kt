package com.anahoret.imagilabsapi.projects.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareDetailsListUseCase
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectDetails
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service
import java.util.*

interface ProjectGetUseCase {

    fun get(getBy: UserProfile, projectId: UUID): Either<OperationError, ProjectDetails>
}

@Service
class ProjectGetUseCaseImpl(
    private val projectService: ProjectService,
    private val projectAccessService: ProjectAccessService,
    private val projectOwnerGetUseCase: ProjectOwnerGetUseCase,
    private val projectClassroomShareDetailsListUseCase: ProjectClassroomShareDetailsListUseCase
) : ProjectGetUseCase {

    override fun get(getBy: UserProfile, projectId: UUID): Either<OperationError, ProjectDetails> {
        val project = projectService.getProjectById(projectId) ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!projectAccessService.canGet(getBy, project))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()
        val canEdit = projectAccessService.canEdit(getBy, project)
        val canUnshare = projectAccessService.canUnshare(getBy, project)
        val classroomShares = projectClassroomShareDetailsListUseCase.getShareDetails(project.id)
        val owner = projectOwnerGetUseCase.get(project)
            ?: return NotFoundError("OWNER_NOT_FOUND").left()
        return ProjectDetails.fromProject(project, owner, canEdit, canUnshare, classroomShares).right()
    }
}
