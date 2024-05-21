package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceEntity
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

interface OpenAiAssistanceService {
    fun save(sessionId: UUID, userId: UUID, projectId: UUID, userQuestion: String, aiResponse: String): OpenAiAssistance
    fun existsBySessionId(sessionId: UUID): Boolean
    fun getById(id: UUID): OpenAiAssistance?
    fun leaveFeedback(id: UUID, isHelpful: Boolean)
}

@Service
class OpenAiAssistanceServiceImpl(
    private val openAiAssistanceRepository: OpenAiAssistanceRepository
) : OpenAiAssistanceService {

    override fun save(
        sessionId: UUID,
        userId: UUID,
        projectId: UUID,
        userQuestion: String,
        aiResponse: String
    ): OpenAiAssistance {
        return openAiAssistanceRepository.save(
            OpenAiAssistanceEntity(
                sessionId = sessionId,
                userid = userId,
                projectId = projectId,
                userQuestion = userQuestion,
                aiResponse = aiResponse
            )
        ).toOpenAiAssistance()
    }

    override fun getById(id: UUID): OpenAiAssistance? {
        return openAiAssistanceRepository.findByIdOrNull(id)?.toOpenAiAssistance()
    }

    override fun leaveFeedback(id: UUID, isHelpful: Boolean) {
        openAiAssistanceRepository.findByIdOrNull(id)?.let {
            it.isHelpful = isHelpful
            openAiAssistanceRepository.save(it)
        }
    }

    override fun existsBySessionId(sessionId: UUID): Boolean {
        return openAiAssistanceRepository.existsBySessionId(sessionId)
    }

    private fun OpenAiAssistanceEntity.toOpenAiAssistance(): OpenAiAssistance {
        return OpenAiAssistance(id!!, userid)
    }
}
