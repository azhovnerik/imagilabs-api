package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.stereotype.Service

interface OpenAiRequestValidator {
    fun validate(
        request: AssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, Unit>
}

@Service
class OpenAiRequestValidatorImpl : OpenAiRequestValidator {

    override fun validate(
        request: AssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, Unit> {
        when (request) {
            is QuestionAssistanceRequest -> if (request.userQuestion.isBlank()) return ValidationError("USER_QUESTION_IS_BLANK").left()
            is ErrorAssistanceRequest -> if (request.errorMessage.isBlank()) return ValidationError("ERROR_MESSAGE_IS_BLANK").left()
            is ProceedAssistanceRequest -> if (request.input.isBlank()) return ValidationError("USER_INPUT_IS_BLANK").left()
        }
        if (request !is ProceedAssistanceRequest && request.userCode.isBlank()) return ValidationError("USER_CODE_IS_BLANK").left()
        return Unit.right()
    }
}
