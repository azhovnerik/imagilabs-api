package com.anahoret.imagilabsapi.utils

import com.anahoret.imagilabsapi.openai.domain.usecases.ErrorAssistanceRequest
import com.anahoret.imagilabsapi.openai.domain.usecases.ProceedAssistanceRequest
import com.anahoret.imagilabsapi.openai.domain.usecases.QuestionAssistanceRequest
import java.util.UUID

object OpenAiRequestGenerator {
    fun createErrorAssistanceRequest(
        sessionId: UUID = UUID.randomUUID(),
        projectId: UUID = UUID.randomUUID(),
        userCode: String = "User code",
        errorMessage: String = "Error"
    ): ErrorAssistanceRequest {
        return ErrorAssistanceRequest(sessionId, projectId, userCode, errorMessage)
    }

    fun createQuestionAssistanceRequest(
        sessionId: UUID = UUID.randomUUID(),
        projectId: UUID = UUID.randomUUID(),
        userCode: String = "User code",
        userQuestion: String = "User question"
    ): QuestionAssistanceRequest {
        return QuestionAssistanceRequest(sessionId, projectId, userCode, userQuestion)
    }

    fun createProceedAssistanceRequest(
        sessionId: UUID = UUID.randomUUID(),
        projectId: UUID = UUID.randomUUID(),
        userInput: String = "User input"
    ): ProceedAssistanceRequest {
        return ProceedAssistanceRequest(sessionId, projectId, userInput)
    }
}
