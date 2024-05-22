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

interface OpenAiRequestValidator {
    fun validate(
        userProfile: UserProfile,
        request: AssistanceRequest
    ): Either<OperationError, Unit>
}

@Service
class OpenAiRequestValidatorImpl(
    private val projectService: ProjectService,
    private val openAiAccessService: OpenAiAccessService,
    private val openAiAssistanceService: OpenAiAssistanceService
) : OpenAiRequestValidator {

    override fun validate(
        userProfile: UserProfile,
        request: AssistanceRequest
    ): Either<OperationError, Unit> {
        when (request) {
            is QuestionAssistanceRequest -> if (request.userQuestion.isBlank()) return ValidationError("USER_QUESTION_IS_BLANK").left()
            is ErrorAssistanceRequest -> if (request.errorMessage.isBlank()) return ValidationError("ERROR_MESSAGE_IS_BLANK").left()
            is ProceedAssistanceRequest -> if (request.input.isBlank()) return ValidationError("USER_INPUT_IS_BLANK").left()
        }
        if (request !is ProceedAssistanceRequest && request.userCode.isBlank()) return ValidationError("USER_CODE_IS_BLANK").left()
        val project = projectService.getProjectById(request.projectId)
            ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!openAiAccessService.canGetAssistance(userProfile, project)) {
            return AccessDeniedError("ACCESS_TO_OPEN_AI_DENIED").left()
        }
        return validateSessionId(request)
    }

    private fun validateSessionId(request: AssistanceRequest): Either<OperationError, Unit> {
        when (request) {
            is ProceedAssistanceRequest -> if (!openAiAssistanceService.existsBySessionId(request.sessionId)) {
                return NotFoundError("SESSION_ID_NOT_FOUND").left()
            }

            else -> if (openAiAssistanceService.existsBySessionId(request.sessionId)) return ValidationError("SESSION_ID_ALREADY_EXISTS").left()
        }
        return Unit.right()
    }
}
