package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError.FieldIsBlank
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError.FieldIsTooLong
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
            is QuestionAssistanceRequest -> validateInput(request.userQuestion, "userQuestion")?.let { return it }
            is ErrorAssistanceRequest -> if (request.errorMessage.isBlank()) return FieldIsBlank("errorMessage").left()
            is ProceedAssistanceRequest -> validateInput(request.input, "input")?.let { return it }
        }
        if (request !is ProceedAssistanceRequest && request.userCode.isBlank()) return FieldIsBlank("userCode").left()
        return Unit.right()
    }

    private fun validateInput(input: String, field: String): Either<OperationError, Unit>? {
        if (input.isBlank()) return FieldIsBlank(field).left()
        if (input.length > 300) return FieldIsTooLong(field).left()
        return null
    }
}
