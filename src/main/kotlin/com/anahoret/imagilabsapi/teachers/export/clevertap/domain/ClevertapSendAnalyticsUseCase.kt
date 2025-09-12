package com.anahoret.imagilabsapi.teachers.export.clevertap.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CONGRATULATION_DIALOG_SHOWN
import com.anahoret.imagilabsapi.teachers.export.clevertap.api.ClevertapAnalyticsApi
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapEvents.COMPLETE_ONBOARDING_STEP
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapEvents.TEACHER_SUBSCRIPTION_EXPIRED
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapTypes.EVENT
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

interface ClevertapSendAnalyticsUseCase {

    fun sendTeachersExpiredSubscriptionEvent(teachersIds: List<UUID>)
    fun sendCompleteOnboardingStepEvent(teacherId: UUID, step: TeacherCheckListStep)
}

@Service
@Profile("prod", "stage")
class ClevertapSendAnalyticsUseCaseImpl(
    private val clevertapAnalyticsApi: ClevertapAnalyticsApi
) : ClevertapSendAnalyticsUseCase {

    override fun sendTeachersExpiredSubscriptionEvent(teachersIds: List<UUID>) {
        teachersIds
            .map { ClevertapRequest(identity = it.toString(), type = EVENT, evtName = TEACHER_SUBSCRIPTION_EXPIRED) }
            .let(clevertapAnalyticsApi::sendAnalytics)
    }

    override fun sendCompleteOnboardingStepEvent(teacherId: UUID, step: TeacherCheckListStep) {

        if (step == CONGRATULATION_DIALOG_SHOWN)
            return

        val request = ClevertapRequest(
            identity = teacherId.toString(),
            type = EVENT,
            evtName = COMPLETE_ONBOARDING_STEP,
            evtData = mapOf("step" to step.clevertapName)
        )
        clevertapAnalyticsApi.sendAnalytics(listOf(request))
    }
}
