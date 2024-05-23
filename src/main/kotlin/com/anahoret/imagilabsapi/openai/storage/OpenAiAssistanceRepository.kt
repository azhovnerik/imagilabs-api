package com.anahoret.imagilabsapi.openai.storage

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface OpenAiAssistanceRepository : CrudRepository<OpenAiAssistanceEntity, UUID> {
    fun existsBySessionId(sessionId: UUID): Boolean

    fun findAllBySessionIdOrderByCreatedAt(sessionId: UUID): List<OpenAiAssistanceContent>
}

interface OpenAiAssistanceContent {
    val userQuestion: String
    val aiResponse: String
    val userCode: String
}
