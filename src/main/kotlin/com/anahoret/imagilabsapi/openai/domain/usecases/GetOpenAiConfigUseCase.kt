package com.anahoret.imagilabsapi.openai.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService.Companion.availableToAllDate
import com.anahoret.imagilabsapi.openai.domain.OpenAiConfig
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.time.Clock

interface GetOpenAiConfigUseCase {
    fun get(userProfile: UserProfile): OpenAiConfig
}

@Service
class GetOpenAiConfigUseCaseImpl(
    private val openAiAccessService: OpenAiAccessService,
    private val openAiService: OpenAiService,
    private val clock: Clock
) : GetOpenAiConfigUseCase {
    override fun get(userProfile: UserProfile): OpenAiConfig {
        val aiChatOnboardingCompleted = when (userProfile) {
            is TeacherProfile -> openAiService.isAiChatOnboardingCompleted(userProfile)
            is StudentProfile -> openAiService.isAiChatOnboardingCompleted(userProfile)
            else -> false
        }
        val aiChatIntroSeen = when (userProfile) {
            is TeacherProfile -> clock.millis() < availableToAllDate || openAiService.isAiChatIntroSeen(userProfile)
            else -> true
        }
        val aiChatAvailable = openAiAccessService.canGetAssistanceForProject(userProfile)
        return OpenAiConfig(aiChatOnboardingCompleted, aiChatAvailable, aiChatIntroSeen)
    }
}
