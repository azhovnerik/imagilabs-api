package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.SECOND_DIRECTIVE
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceOnSuccessResponse
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceOnSuccessRequest
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service

interface GetOpenAiAssistanceOnSuccessUseCase {
    fun get(
        userProfile: UserProfile,
        request: AssistanceOnSuccessRequest
    ): Either<OperationError, AssistanceOnSuccessResponse>
}

@Service
class GetOpenAiAssistanceOnSuccessUseCaseImpl(
    private val projectService: ProjectService,
    private val openAiAccessService: OpenAiAccessService,
    private val openAiService: OpenAiService,
    private val openAiAssistanceService: OpenAiAssistanceService
) : GetOpenAiAssistanceOnSuccessUseCase {

    override fun get(
        userProfile: UserProfile,
        request: AssistanceOnSuccessRequest
    ): Either<OperationError, AssistanceOnSuccessResponse> {
        if (request.userQuestion.isBlank()) return ValidationError("USER_QUESTION_IS_BLANK").left()
        if (request.userCode.isBlank()) return ValidationError("USER_CODE_IS_BLANK").left()
        val project =
            projectService.getProjectById(request.projectId) ?: return NotFoundError("PROJECT_NOT_FOUND").left()
        if (!openAiAccessService.canGetAssistance(
                userProfile,
                project
            )
        ) return AccessDeniedError("ACCESS_TO_OPEN_AI_DENIED").left()
        val secondDirectiveWithQuestion = "My question is: ${request.userQuestion} $SECOND_DIRECTIVE"
        val aiResponse =
            openAiService.getAssistanceOnSuccess(request, secondDirectiveWithQuestion).results[0].output.content
        openAiAssistanceService.save(userProfile.id, project.id, request.userQuestion, aiResponse)
        return AssistanceOnSuccessResponse(aiResponse).right()
    }
}
