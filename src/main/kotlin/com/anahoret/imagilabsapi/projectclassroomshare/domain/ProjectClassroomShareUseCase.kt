package com.anahoret.imagilabsapi.projectclassroomshare.domain

import arrow.core.Either
import arrow.core.filterOrElse
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.pythoncompiler.domain.CodeRunUseCase
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeRequest
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse
import com.anahoret.imagilabsapi.userclassroomlink.domain.UserClassroomLinkService
import org.springframework.stereotype.Service

interface ProjectClassroomShareUseCase {

    fun share(
        sharedBy: UserProfile,
        projectClassroomShareChangeRequest: ProjectClassroomShareChangeRequest
    ): Either<OperationError, RunCodeResponse>
}

@Service
class ProjectClassroomShareUseCaseImpl(
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val projectService: ProjectService,
    private val userClassroomLinkService: UserClassroomLinkService,
    private val projectAccessService: ProjectAccessService,
    private val codeRunUseCase: CodeRunUseCase
) : ProjectClassroomShareUseCase {

    override fun share(
        sharedBy: UserProfile,
        projectClassroomShareChangeRequest: ProjectClassroomShareChangeRequest
    ): Either<OperationError, RunCodeResponse> {
        val projectId = projectClassroomShareChangeRequest.projectId
        val classroomIds = projectClassroomShareChangeRequest.classroomIds

        val project = projectService.getProjectById(projectId)
            ?: return NotFoundError("PROJECT_NOT_FOUND").left()

        if (!projectAccessService.canShare(sharedBy, project))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()

        if (classroomIds.any { !userClassroomLinkService.isLinkedToClassroom(sharedBy, it) })
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        val result = codeRunUseCase.run(sharedBy, RunCodeRequest(project.sourceCode))
            .filterOrElse({ it.output != null }) { ValidationError("PROJECT_COMPILATION_FAILED") }
        return when (result) {
            is Either.Left -> result
            is Either.Right -> {
                projectClassroomShareService.shareToAll(projectId, classroomIds)
                projectService.updateLastModifiedDate(projectId)
                result.value.right()
            }
        }
    }

}
