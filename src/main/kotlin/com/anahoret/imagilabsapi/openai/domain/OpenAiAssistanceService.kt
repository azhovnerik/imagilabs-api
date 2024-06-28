package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.usecases.AssistanceRequest
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceContent
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceEntity
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface OpenAiAssistanceService {
    fun save(
        userProfile: UserProfile,
        userQuestion: String,
        aiResponse: String,
        request: AssistanceRequest
    ): OpenAiAssistance

    fun existsBySessionId(sessionId: UUID): Boolean
    fun getById(id: UUID): OpenAiAssistance?
    fun leaveFeedback(id: UUID, isHelpful: Boolean)
    fun getAllBySessionId(sessionId: UUID): List<OpenAiAssistanceContent>
}

@Service
class OpenAiAssistanceServiceImpl(
    private val openAiAssistanceRepository: OpenAiAssistanceRepository
) : OpenAiAssistanceService {

    override fun save(
        userProfile: UserProfile,
        userQuestion: String,
        aiResponse: String,
        request: AssistanceRequest
    ): OpenAiAssistance {
        return openAiAssistanceRepository.save(
            OpenAiAssistanceEntity(
                sessionId = request.sessionId,
                userId = userProfile.id,
                projectId = request.projectId,
                userQuestion = userQuestion,
                aiResponse = aiResponse,
                userCode = request.userCode,
                userType = userProfile.userType
            )
        ).toOpenAiAssistance()
    }

    override fun existsBySessionId(sessionId: UUID): Boolean {
        return openAiAssistanceRepository.existsBySessionId(sessionId)
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

    override fun getAllBySessionId(sessionId: UUID): List<OpenAiAssistanceContent> {
        return openAiAssistanceRepository.findAllBySessionIdOrderByCreatedAt(sessionId)
    }

    private fun OpenAiAssistanceEntity.toOpenAiAssistance(): OpenAiAssistance {
        return OpenAiAssistance(id!!, userId, userType)
    }
}
