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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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

    @GetMapping(ON_SUCCESS_PATH)
    fun assistOnSuccess(
        @AuthenticationPrincipal userProfile: UserProfile,
        @RequestBody request: QuestionAssistanceRequest
    ): ResponseEntity<ResponseDto<AssistanceResponse>> {
        return when (val result = getOpenAiResponseOnSuccess.get(userProfile, request)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @GetMapping(ON_ERROR_START_PATH)
    fun startAssistanceOnError(
        @AuthenticationPrincipal userProfile: UserProfile,
        @RequestBody request: ErrorAssistanceRequest
    ): ResponseEntity<ResponseDto<AssistanceResponse>> {
        return when (val result = startOpenAiAssistanceOnErrorUseCase.getAssistance(userProfile, request)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @GetMapping(ON_ERROR_PROCEED_PATH)
    fun proceedAssistanceOnError(
        @AuthenticationPrincipal userProfile: UserProfile,
        @RequestBody request: ProceedAssistanceRequest
    ): ResponseEntity<ResponseDto<AssistanceResponse>> {
        return when (val result = proceedOpenAiAssistanceOnErrorUseCase.getAssistance(userProfile, request)) {
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
