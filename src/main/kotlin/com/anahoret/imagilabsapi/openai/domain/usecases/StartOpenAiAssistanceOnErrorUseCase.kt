package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceResponse
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
    private val openAiAssistanceService: OpenAiAssistanceService
) : StartOpenAiAssistanceOnErrorUseCase {

    override fun getAssistance(
        request: ErrorAssistanceRequest,
        userProfile: UserProfile
    ): Either<OperationError, AssistanceResponse> {
        return openAiRequestValidator.validate(userProfile, request).map {
            val userQuestion = "I am receiving this error: ${request.errorMessage}"
            val secondDirectiveWithError = "${OpenAiPrompts.SECOND_DIRECTIVE} $userQuestion"
            val aiResponse = openAiService.startAssistance(request.userCode, secondDirectiveWithError)
            val openAiAssistance = openAiAssistanceService.save(
                userId = userProfile.id,
                userQuestion = userQuestion,
                aiResponse = aiResponse,
                request = request
            )
            AssistanceResponse(openAiAssistance.id, aiResponse)
        }
    }
}
