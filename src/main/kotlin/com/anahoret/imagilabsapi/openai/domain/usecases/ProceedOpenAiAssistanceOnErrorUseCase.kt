package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.flatMap
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceResponse
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
    private val openAiPreconditionChecker: OpenAiPreconditionChecker
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
        val allAssistance = openAiAssistanceService.getAllBySessionId(request.sessionId)
        val aiResponse = openAiService.proceedAssistanceOnError(request.input, allAssistance)
        val openAiAssistance = openAiAssistanceService.save(userProfile.id, request.input, aiResponse, request)
        return AssistanceResponse(openAiAssistance.id, aiResponse)
    }
}
