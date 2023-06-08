package com.anahoret.imagilabsapi.projects.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service
import java.util.*
import jakarta.transaction.Transactional

interface ProjectDeleteUseCase {

    fun delete(classroomId: UUID?, deleteBy: UserProfile, projectId: UUID): Either<OperationError, Unit>
}

@Service
class ProjectDeleteUseCaseImpl(
    private val projectService: ProjectService,
    private val projectAccessService: ProjectAccessService,
    private val projectClassroomShareService: ProjectClassroomShareService
) : ProjectDeleteUseCase {

    @Transactional(rollbackOn = [Throwable::class])
    override fun delete(classroomId: UUID?, deleteBy: UserProfile, projectId: UUID): Either<OperationError, Unit> {
        val project = projectService.getProjectById(projectId)
            ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!projectAccessService.canDelete(deleteBy, project, classroomId))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()
        projectClassroomShareService.unshareFromAll(project.id)
        projectService.delete(project.id)
        return Unit.right()
    }
}
