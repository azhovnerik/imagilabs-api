package com.anahoret.imagilabsapi.utils

import com.anahoret.imagilabsapi.openai.domain.usecases.ErrorAssistanceRequest
import com.anahoret.imagilabsapi.openai.domain.usecases.ProceedAssistanceRequest
import com.anahoret.imagilabsapi.openai.domain.usecases.QuestionAssistanceRequest
import java.util.*

object OpenAiRequestGenerator {
    fun createErrorAssistanceRequest(
        sessionId: UUID = UUID.randomUUID(),
        userCode: String = "User code",
        errorMessage: String = "Error"
    ): ErrorAssistanceRequest {
        return ErrorAssistanceRequest(sessionId, userCode, errorMessage)
    }

    fun createQuestionAssistanceRequest(
        sessionId: UUID = UUID.randomUUID(),
        userCode: String = "User code",
        userQuestion: String = "User question"
    ): QuestionAssistanceRequest {
        return QuestionAssistanceRequest(sessionId, userCode, userQuestion)
    }

    fun createProceedAssistanceRequest(
        sessionId: UUID = UUID.randomUUID(),
        userCode: String = "User code",
        userInput: String = "User input"
    ): ProceedAssistanceRequest {
        return ProceedAssistanceRequest(sessionId, userCode, userInput)
    }
}
