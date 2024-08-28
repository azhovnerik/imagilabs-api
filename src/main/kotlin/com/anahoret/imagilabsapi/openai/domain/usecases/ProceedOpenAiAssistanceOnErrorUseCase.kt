package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.flatMap
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.*
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceContent
import org.springframework.stereotype.Service

interface ProceedOpenAiAssistanceOnErrorUseCase {
    fun getAssistance(
        request: ProceedAssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, AssistanceResponse>
}

@Service
class ProceedOpenAiAssistanceOnErrorUseCaseImpl(
    private val openAiRequestValidator: OpenAiRequestValidator,
    private val openAiAssistanceService: OpenAiAssistanceService,
    private val openAiService: OpenAiService,
    private val openAiPreconditionChecker: OpenAiPreconditionChecker,
    private val tipTokensService: TipTokensService
) : ProceedOpenAiAssistanceOnErrorUseCase {

    override fun getAssistance(
        request: ProceedAssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, AssistanceResponse> {
        return openAiRequestValidator.validate(request, userProfile)
            .flatMap { openAiPreconditionChecker.check(request, userProfile) }
            .map { proceedAssistance(request, userProfile) }
    }

    private fun proceedAssistance(
        request: ProceedAssistanceRequest,
        userProfile: UserProfile
    ): AssistanceResponse {
        val userQuestion = OpenAiPrompts.QUESTION_DIRECTIVE
            .replace("{user_input}", request.input)
            .replace("{user_code}", request.userCode)
        val allAssistance = openAiAssistanceService.getAllBySessionId(request.sessionId)
        val aiResponse = openAiService.proceedAssistanceOnError(userQuestion, allAssistance)
        val openAiAssistance = openAiAssistanceService.save(userProfile, userQuestion, aiResponse, request)
        tipTokensService.withdrawOneTipToken(userProfile)
        return AssistanceResponse(openAiAssistance.id, aiResponse)
    }
}
