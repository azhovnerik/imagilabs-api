package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import org.springframework.stereotype.Service

interface OpenAiPreconditionChecker {
    fun check(request: AssistanceRequest, userProfile: UserProfile): Either<OperationError, Unit>
}

@Service
class OpenAiPreconditionCheckerImpl(
    private val openAiAccessService: OpenAiAccessService,
) : OpenAiPreconditionChecker {

    override fun check(request: AssistanceRequest, userProfile: UserProfile): Either<OperationError, Unit> {
        if (!openAiAccessService.canGetAssistanceForProject(userProfile)) {
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()
        }
        if (!openAiAccessService.hasTipTokens(userProfile)) return AccessDeniedError("NO_TIP_TOKENS_LEFT").left()
        return Unit.right()
    }
}
