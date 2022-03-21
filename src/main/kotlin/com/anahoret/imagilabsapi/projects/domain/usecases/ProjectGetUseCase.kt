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
import org.springframework.stereotype.Service
import java.util.*

interface ProjectGetUseCase {

    fun get(getBy: UserProfile, projectId: UUID): Either<OperationError, ProjectDetails>
}

@Service
class ProjectGetUseCaseImpl(
    private val projectService: ProjectService,
    private val projectAccessService: ProjectAccessService
) : ProjectGetUseCase {

    override fun get(getBy: UserProfile, projectId: UUID): Either<OperationError, ProjectDetails> {
        val project = projectService.getProjectById(projectId) ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!projectAccessService.canGet(getBy, project))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()
        val canEdit = projectAccessService.canEdit(getBy, project)
        return ProjectDetails.fromProject(project, canEdit).right()
    }
}
