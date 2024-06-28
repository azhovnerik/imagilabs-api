package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service

interface OpenAiPreconditionChecker {
    fun check(request: AssistanceRequest, userProfile: UserProfile): Either<OperationError, Unit>
}

@Service
class OpenAiPreconditionCheckerImpl(
    private val projectService: ProjectService,
    private val openAiAccessService: OpenAiAccessService,
    private val openAiAssistanceService: OpenAiAssistanceService
) : OpenAiPreconditionChecker {

    override fun check(request: AssistanceRequest, userProfile: UserProfile): Either<OperationError, Unit> {
        val project = projectService.getProjectById(request.projectId)
            ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!openAiAccessService.canGetAssistanceForProject(userProfile, project)) {
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()
        }
        if (!openAiAccessService.hasTipTokens(userProfile)) return AccessDeniedError("NO_TIP_TOKENS_LEFT").left()
        if (request is ProceedAssistanceRequest && !openAiAssistanceService.existsBySessionId(request.sessionId)) {
            return NotFoundError("SESSION_ID_NOT_FOUND").left()
        }
        if (request is ProceedAssistanceRequest) return Unit.right()
        if (openAiAssistanceService.existsBySessionId(request.sessionId)) return ValidationError(
            "SESSION_ID_ALREADY_EXISTS"
        ).left()
        return Unit.right()
    }
}
