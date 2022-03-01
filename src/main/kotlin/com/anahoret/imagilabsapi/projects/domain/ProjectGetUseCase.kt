package com.anahoret.imagilabsapi.projects.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import org.springframework.stereotype.Service
import java.util.*

interface ProjectGetUseCase {

    fun get(userProfile: UserProfile, projectId: UUID): Either<OperationError, Project>
}

@Service
class ProjectGetUseCaseImpl(
    private val projectService: ProjectService,
    private val projectAccessService: ProjectAccessService
) : ProjectGetUseCase {

    override fun get(userProfile: UserProfile, projectId: UUID): Either<OperationError, Project> {
        val project = projectService.getProjectById(projectId) ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!projectAccessService.canGet(userProfile, project))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()
        return project.right()
    }
}
