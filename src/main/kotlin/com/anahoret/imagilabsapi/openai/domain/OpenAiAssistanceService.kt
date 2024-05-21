package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceEntity
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceRepository
import org.springframework.stereotype.Service
import java.util.UUID

interface OpenAiAssistanceService {
    fun save(sessionId: UUID, userId: UUID, projectId: UUID, userQuestion: String, aiResponse: String)
    fun existsBySessionId(sessionId: UUID): Boolean
}

@Service
class OpenAiAssistanceServiceImpl(
    private val openAiAssistanceRepository: OpenAiAssistanceRepository
) : OpenAiAssistanceService {

    override fun save(sessionId: UUID, userId: UUID, projectId: UUID, userQuestion: String, aiResponse: String) {
        openAiAssistanceRepository.save(
            OpenAiAssistanceEntity(
                sessionId = sessionId,
                userid = userId,
                projectId = projectId,
                userQuestion = userQuestion,
                aiResponse = aiResponse
            )
        )
    }

    override fun existsBySessionId(sessionId: UUID): Boolean {
        return openAiAssistanceRepository.existsBySessionId(sessionId)
    }
}
