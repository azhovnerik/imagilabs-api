package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceResponse
import org.springframework.stereotype.Service

interface ProceedOpenAiAssistanceOnErrorUseCase {
    fun getAssistance(
        userProfile: UserProfile,
        request: ProceedAssistanceRequest
    ): Either<OperationError, AssistanceResponse>
}

@Service
class ProceedOpenAiAssistanceOnErrorUseCaseImpl(
    private val openAiRequestValidator: OpenAiRequestValidator,
    private val openAiAssistanceService: OpenAiAssistanceService,
    private val openAiService: OpenAiService
) : ProceedOpenAiAssistanceOnErrorUseCase {
    override fun getAssistance(
        userProfile: UserProfile,
        request: ProceedAssistanceRequest
    ): Either<OperationError, AssistanceResponse> {
        return openAiRequestValidator.validate(userProfile, request).map {
            val allAssistance = openAiAssistanceService.getAllBySessionId(request.sessionId)
            val aiResponse = openAiService.proceedAssistanceOnError(request.input, allAssistance)
            val openAiAssistance = openAiAssistanceService.save(userProfile.id, request.input, aiResponse, request)
            AssistanceResponse(openAiAssistance.id, aiResponse)
        }
    }
}
