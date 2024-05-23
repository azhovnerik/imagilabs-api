package com.anahoret.imagilabsapi.openai.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.openai.domain.usecases.*
import com.anahoret.imagilabsapi.security.UserRole
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@Secured(UserRole.student)
@RestController
class OpenAiController(
    private val getOpenAiResponseOnSuccess: GetOpenAiAssistanceOnSuccessUseCase,
    private val leaveFeedbackUseCase: LeaveFeedbackUseCase,
    private val startOpenAiAssistanceOnErrorUseCase: StartOpenAiAssistanceOnErrorUseCase,
    private val proceedOpenAiAssistanceOnErrorUseCase: ProceedOpenAiAssistanceOnErrorUseCase
) {

    companion object {
        const val ON_SUCCESS_PATH = "/api/open-ai/assistance/on-success"
        const val ON_ERROR_START_PATH = "/api/open-ai/assistance/on-error/start"
        const val ON_ERROR_PROCEED_PATH = "/api/open-ai/assistance/on-error/proceed"
        const val FEEDBACK_PATH = "/api/open-ai/assistance/feedback/{assistanceId}"
    }

    @PostMapping(ON_SUCCESS_PATH)
    fun assistOnSuccess(
        @RequestBody request: QuestionAssistanceRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<AssistanceResponse>> {
        return when (val result = getOpenAiResponseOnSuccess.get(request, userProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @PostMapping(ON_ERROR_START_PATH)
    fun startAssistanceOnError(
        @RequestBody request: ErrorAssistanceRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<AssistanceResponse>> {
        return when (val result = startOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @PostMapping(ON_ERROR_PROCEED_PATH)
    fun proceedAssistanceOnError(
        @RequestBody request: ProceedAssistanceRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<AssistanceResponse>> {
        return when (val result = proceedOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @PostMapping(FEEDBACK_PATH)
    fun leaveFeedback(
        @PathVariable assistanceId: UUID,
        @RequestBody request: LeaveFeedbackRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<Unit>> {
        return when (val result = leaveFeedbackUseCase.leaveFeedback(assistanceId, request.isHelpful, userProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    class AssistanceResponse(
        val assistanceId: UUID,
        val aiResponse: String
    )

    class LeaveFeedbackRequest(
        val isHelpful: Boolean
    )
}
