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
            val userQuestion = "My question is: ${request.userQuestion}"
            val secondDirectiveWithQuestion = "$userQuestion $SECOND_DIRECTIVE"
            val aiResponse = openAiService.startAssistance(request.userCode, secondDirectiveWithQuestion)
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
