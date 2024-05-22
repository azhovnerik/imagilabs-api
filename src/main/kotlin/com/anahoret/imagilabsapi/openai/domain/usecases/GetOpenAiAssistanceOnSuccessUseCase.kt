package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.SECOND_DIRECTIVE
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceResponse
import org.springframework.stereotype.Service

interface GetOpenAiAssistanceOnSuccessUseCase {
    fun get(
        userProfile: UserProfile,
        request: QuestionAssistanceRequest
    ): Either<OperationError, AssistanceResponse>
}

@Service
class GetOpenAiAssistanceOnSuccessUseCaseImpl(
    private val openAiService: OpenAiService,
    private val openAiAssistanceService: OpenAiAssistanceService,
    private val openAiRequestValidator: OpenAiRequestValidator
) : GetOpenAiAssistanceOnSuccessUseCase {

    override fun get(
        userProfile: UserProfile,
        request: QuestionAssistanceRequest
    ): Either<OperationError, AssistanceResponse> {
        return openAiRequestValidator.validate(userProfile, request).map {
            val secondDirectiveWithQuestion = "My question is: ${request.userQuestion} $SECOND_DIRECTIVE"
            val aiResponse =
                openAiService.startAssistance(request.userCode, secondDirectiveWithQuestion).results[0].output.content
            val openAiAssistance = openAiAssistanceService.save(
                sessionId = request.sessionId,
                userId = userProfile.id,
                projectId = request.projectId,
                userQuestion = request.userQuestion,
                aiResponse = aiResponse
            )
            AssistanceResponse(openAiAssistance.id, aiResponse)
        }
    }
}
