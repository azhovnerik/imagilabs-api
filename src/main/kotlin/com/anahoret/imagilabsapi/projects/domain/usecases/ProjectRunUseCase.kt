package com.anahoret.imagilabsapi.projects.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.pythoncompiler.PythonCompilerService
import com.anahoret.imagilabsapi.pythoncompiler.RunCodeResponse
import org.springframework.stereotype.Service
import java.util.*

interface ProjectRunUseCase {

    fun run(runBy: UserProfile, projectId: UUID): Either<OperationError, RunCodeResponse>
}

@Service
class ProjectRunUseCaseImpl(
    private val projectService: ProjectService,
    private val projectAccessService: ProjectAccessService,
    private val pythonCompilerService: PythonCompilerService
) : ProjectRunUseCase {

    override fun run(runBy: UserProfile, projectId: UUID): Either<OperationError, RunCodeResponse> {
        val project = projectService.getProjectById(projectId) ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!projectAccessService.canRun(runBy, project))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()

        val runCodeResponse = pythonCompilerService.runCode(project.sourceCode)
        projectService.updateProjectRunResult(project.id, runCodeResponse)
        return runCodeResponse.right()
    }

}
