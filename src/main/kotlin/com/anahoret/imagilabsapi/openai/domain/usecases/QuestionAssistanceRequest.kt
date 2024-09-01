package com.anahoret.imagilabsapi.openai.domain.usecases

import java.util.*

abstract class AssistanceRequest(
    open val sessionId: UUID,
    open val userCode: String
)

class QuestionAssistanceRequest(
    override val sessionId: UUID,
    override val userCode: String,
    val userQuestion: String
) : AssistanceRequest(sessionId, userCode)

class ErrorAssistanceRequest(
    override val sessionId: UUID,
    override val userCode: String,
    val errorMessage: String
) : AssistanceRequest(sessionId, userCode)

class ProceedAssistanceRequest(
    override val sessionId: UUID,
    override val userCode: String,
    val input: String
) : AssistanceRequest(sessionId, userCode)
