package com.anahoret.imagilabsapi.projects.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectDetails
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.projects.domain.ProjectUpdateRequest
import org.springframework.stereotype.Service
import java.util.*

interface ProjectUpdateUseCase {

    fun update(
        updateBy: UserProfile,
        projectId: UUID,
        projectUpdateRequest: ProjectUpdateRequest
    ): Either<OperationError, ProjectDetails>
}

@Service
class ProjectUpdateUseCaseImpl(
    private val projectService: ProjectService,
    private val projectAccessService: ProjectAccessService
) : ProjectUpdateUseCase {

    override fun update(
        updateBy: UserProfile,
        projectId: UUID,
        projectUpdateRequest: ProjectUpdateRequest
    ): Either<OperationError, ProjectDetails> {
        val project = projectService.getProjectById(projectId) ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!projectAccessService.canEdit(updateBy, project))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()
        return projectService.updateProject(projectId, projectUpdateRequest)
            ?.let { ProjectDetails.fromProject(it, canEdit = true) }
            ?.right()
            ?: NotFoundError("PROJECT_NOT_FOUND").left()
    }
}
