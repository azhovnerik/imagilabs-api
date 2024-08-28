package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.flatMap
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.*
import org.springframework.stereotype.Service

interface GetOpenAiAssistanceOnSuccessUseCase {
    fun get(
        request: QuestionAssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, AssistanceResponse>
}

@Service
class GetOpenAiAssistanceOnSuccessUseCaseImpl(
    private val openAiService: OpenAiService,
    private val openAiAssistanceService: OpenAiAssistanceService,
    private val openAiRequestValidator: OpenAiRequestValidator,
    private val openAiPreconditionChecker: OpenAiPreconditionChecker,
    private val tipTokensService: TipTokensService
) : GetOpenAiAssistanceOnSuccessUseCase {

    override fun get(
        request: QuestionAssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, AssistanceResponse> {
        return openAiRequestValidator.validate(request, userProfile)
            .flatMap { openAiPreconditionChecker.check(request, userProfile) }
            .map { startAssistance(request, userProfile) }
    }

    private fun startAssistance(
        request: QuestionAssistanceRequest,
        userProfile: UserProfile
    ): AssistanceResponse {
        val userQuestion = OpenAiPrompts.QUESTION_DIRECTIVE
            .replace("{user_input}", request.userQuestion)
            .replace("{user_code}", request.userCode)
        val aiResponse = openAiService.startAssistance(userQuestion)
        val openAiAssistance = openAiAssistanceService.save(userProfile, userQuestion, aiResponse, request)
        tipTokensService.withdrawOneTipToken(userProfile)
        return AssistanceResponse(openAiAssistance.id, aiResponse)
    }
}
