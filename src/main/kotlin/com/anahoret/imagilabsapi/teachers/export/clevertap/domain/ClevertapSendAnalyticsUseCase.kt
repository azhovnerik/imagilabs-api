package com.anahoret.imagilabsapi.teachers.export.clevertap.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CONGRATULATION_DIALOG_SHOWN
import com.anahoret.imagilabsapi.teachers.export.clevertap.api.ClevertapAnalyticsApi
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapEvents.completeOnboardingStep
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapEvents.teacherSubscriptionExpired
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapTypes.event
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

interface ClevertapSendAnalyticsUseCase {

    fun sendTeachersExpiredSubscriptionEvent(teachersIds: List<UUID>)
    fun sendCompleteOnboardingStepEvent(step: TeacherCheckListStep)
}

@Service
@Profile("prod", "stage")
class ClevertapSendAnalyticsUseCaseImpl(
    private val clevertapAnalyticsApi: ClevertapAnalyticsApi
) : ClevertapSendAnalyticsUseCase {

    override fun sendTeachersExpiredSubscriptionEvent(teachersIds: List<UUID>) {
        teachersIds
            .map { ClevertapRequest(it.toString(), event, teacherSubscriptionExpired) }
            .let(clevertapAnalyticsApi::sendAnalytics)
    }

    override fun sendCompleteOnboardingStepEvent(step: TeacherCheckListStep) {

        if (step == CONGRATULATION_DIALOG_SHOWN)
            return

        val request = ClevertapRequest(step.clevertapName, event, completeOnboardingStep)
        clevertapAnalyticsApi.sendAnalytics(listOf(request))
    }
}
