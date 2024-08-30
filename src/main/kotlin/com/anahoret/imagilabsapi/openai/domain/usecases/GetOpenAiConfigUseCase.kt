package com.anahoret.imagilabsapi.openai.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.openai.domain.OpenAiConfig
import com.anahoret.imagilabsapi.openai.domain.OpenAiUser
import org.springframework.stereotype.Service

interface GetOpenAiConfigUseCase {
    fun get(userProfile: UserProfile): OpenAiConfig
}

@Service
class GetOpenAiConfigUseCaseImpl(
    private val openAiAccessService: OpenAiAccessService
) : GetOpenAiConfigUseCase {
    override fun get(userProfile: UserProfile): OpenAiConfig {
        val aiChatOnboardingCompleted = (userProfile as? OpenAiUser)?.aiChatOnboardingCompleted ?: false
        val aiChatAvailable = openAiAccessService.canGetAssistanceForProject(userProfile)
        return OpenAiConfig(aiChatOnboardingCompleted, aiChatAvailable)
    }
}
