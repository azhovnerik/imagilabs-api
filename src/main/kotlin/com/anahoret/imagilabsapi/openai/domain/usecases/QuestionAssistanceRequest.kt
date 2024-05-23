package com.anahoret.imagilabsapi.openai.domain.usecases

import java.util.*

abstract class AssistanceRequest(
    open val sessionId: UUID,
    open val projectId: UUID,
    open val userCode: String
)

class QuestionAssistanceRequest(
    override val sessionId: UUID,
    override val projectId: UUID,
    override val userCode: String,
    val userQuestion: String
) : AssistanceRequest(sessionId, projectId, userCode)

class ErrorAssistanceRequest(
    override val sessionId: UUID,
    override val projectId: UUID,
    override val userCode: String,
    val errorMessage: String
) : AssistanceRequest(sessionId, projectId, userCode)

class ProceedAssistanceRequest(
    override val sessionId: UUID,
    override val projectId: UUID,
    val input: String
) : AssistanceRequest(sessionId, projectId, "")
