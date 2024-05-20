package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceEntity
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceRepository
import org.springframework.stereotype.Service
import java.util.UUID

interface OpenAiAssistanceService {
    fun save(userId: UUID, projectId: UUID, userQuestion: String, aiResponse: String)
}

@Service
class OpenAiAssistanceServiceImpl(
    private val openAiAssistanceRepository: OpenAiAssistanceRepository
) : OpenAiAssistanceService {
    override fun save(userId: UUID, projectId: UUID, userQuestion: String, aiResponse: String) {
        openAiAssistanceRepository.save(
            OpenAiAssistanceEntity(
                userid = userId, projectId = projectId, userQuestion = userQuestion, aiResponse = aiResponse
            )
        )
    }
}
