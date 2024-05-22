package com.anahoret.imagilabsapi.openai.storage

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface OpenAiAssistanceRepository : CrudRepository<OpenAiAssistanceEntity, UUID> {
    fun existsBySessionId(sessionId: UUID): Boolean

    @Query("""
        SELECT oa.userQuestion AS userQuestion, 
                oa.aiResponse AS aiResponse, 
                oa.userCode AS userCode
        FROM OpenAiAssistanceEntity AS oa
        WHERE oa.sessionId = :sessionId
        ORDER BY oa.createdAt
    """)
    fun findAllBySessionIdAndOrderByCreatedAt(sessionId: UUID): List<OpenAiAssistanceContent>
}

interface OpenAiAssistanceContent {
    val userQuestion: String
    val aiResponse: String
    val userCode: String
}
