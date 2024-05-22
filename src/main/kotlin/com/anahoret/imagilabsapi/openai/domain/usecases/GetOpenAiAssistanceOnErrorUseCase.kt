package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceResponse
import org.springframework.stereotype.Service

interface GetOpenAiAssistanceOnErrorUseCase {
    fun get(userProfile: UserProfile, request: ErrorAssistanceRequest): Either<OperationError, AssistanceResponse>
}

@Service
class GetOpenAiAssistanceOnErrorUseCaseImpl(
    private val openAiRequestValidator: OpenAiRequestValidator,
    private val openAiService: OpenAiService,
    private val openAiAssistanceService: OpenAiAssistanceService
) : GetOpenAiAssistanceOnErrorUseCase {

    override fun get(
        userProfile: UserProfile,
        request: ErrorAssistanceRequest
    ): Either<OperationError, AssistanceResponse> {
        return openAiRequestValidator.validate(userProfile, request).map {
            val secondDirectiveWithQuestion =
                "${OpenAiPrompts.SECOND_DIRECTIVE} I am receiving this error: ${request.errorMessage}"
            val aiResponse = openAiService.startAssistance(
                request.userCode,
                secondDirectiveWithQuestion
            ).results[0].output.content
            val openAiAssistance = openAiAssistanceService.save(
                sessionId = request.sessionId,
                userId = userProfile.id,
                projectId = request.projectId,
                userQuestion = request.errorMessage, //todo add errorMessage into Db
                aiResponse = aiResponse
            )
            AssistanceResponse(openAiAssistance.id, aiResponse)
        }
    }
}
