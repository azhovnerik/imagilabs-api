package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.flatMap
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.*
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.stereotype.Service

interface StartOpenAiAssistanceOnErrorUseCase {
    fun getAssistance(
        request: ErrorAssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, AssistanceResponse>
}

@Service
class StartOpenAiAssistanceOnErrorUseCaseImpl(
    private val openAiRequestValidator: OpenAiRequestValidator,
    private val openAiService: OpenAiService,
    private val openAiAssistanceService: OpenAiAssistanceService,
    private val openAiPreconditionChecker: OpenAiPreconditionChecker,
    private val tipTokensService: TipTokensService
) : StartOpenAiAssistanceOnErrorUseCase {

    override fun getAssistance(
        request: ErrorAssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, AssistanceResponse> {
        return openAiRequestValidator.validate(request, userProfile)
            .flatMap { openAiPreconditionChecker.check(request, userProfile) }
            .map { startAssistance(request, userProfile) }
    }

    private fun startAssistance(
        request: ErrorAssistanceRequest,
        userProfile: UserProfile
    ): AssistanceResponse {
        val userQuestion =
            "I am receiving this error: ${request.errorMessage} ${OpenAiPrompts.ERROR_FIRST_ANSWER_DIRECTIVE}"
        val secondDirectiveWithError = "${OpenAiPrompts.SECOND_DIRECTIVE} $userQuestion"
        val chatOptions: OpenAiChatOptions = OpenAiChatOptions()
            .apply { responseFormat = OpenAiApi.ChatCompletionRequest.ResponseFormat("json_object") }
        val aiResponse = openAiService.startAssistance(request.userCode, secondDirectiveWithError, chatOptions)
        val openAiAssistance = openAiAssistanceService.save(userProfile, userQuestion, aiResponse, request)
        tipTokensService.withdrawOneTipToken(userProfile)
        return AssistanceResponse(openAiAssistance.id, aiResponse)
    }
}
