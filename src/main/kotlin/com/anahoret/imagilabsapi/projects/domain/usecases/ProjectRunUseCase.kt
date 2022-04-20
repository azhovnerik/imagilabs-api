package com.anahoret.imagilabsapi.projects.domain.usecases

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.pythoncompiler.domain.CodeRunUseCase
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeRequest
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse
import org.springframework.stereotype.Service
import java.util.*

interface ProjectRunUseCase {

    fun run(runBy: UserProfile, projectId: UUID): Either<OperationError, RunCodeResponse>
}

@Service
class ProjectRunUseCaseImpl(
    private val projectService: ProjectService,
    private val projectAccessService: ProjectAccessService,
    private val codeRunUseCase: CodeRunUseCase
) : ProjectRunUseCase {

    override fun run(runBy: UserProfile, projectId: UUID): Either<OperationError, RunCodeResponse> {
        val project = projectService.getProjectById(projectId) ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!projectAccessService.canRun(runBy, project))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()

        return codeRunUseCase.run(runBy, RunCodeRequest(project.sourceCode))
            .tap { projectService.updateProjectRunResult(project.id, it) }
    }

}
