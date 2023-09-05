package com.anahoret.imagilabsapi.teachers.export.clevertap.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.*
import com.anahoret.imagilabsapi.teachers.export.clevertap.api.ClevertapAnalyticsApi
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapEvents.completeOnboardingStep
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapEvents.teacherSubscriptionExpired
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapTypes.event
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

interface ClevertapSendAnalyticsUseCase {

    fun sendTeachersExpiredSubscriptionEvent(teachersIds: List<UUID>)
    fun sendCompleteOnboardingStepEvent(teacherCheckListStep: TeacherCheckListStep)
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

    override fun sendCompleteOnboardingStepEvent(teacherCheckListStep: TeacherCheckListStep) {
        val identity = when (teacherCheckListStep) {
            CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM -> OnboardingStepEventNames.createOrJoinClassroom
            SHARE_STUDENT_ACCESS_CODE -> OnboardingStepEventNames.shareStudentCredentials
            EXPLORE_YOUR_FIRST_LESSON -> OnboardingStepEventNames.exploreLesson
            CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP -> OnboardingStepEventNames.checkFacebookGroup
            CREATE_YOUR_FIRST_PROJECT -> OnboardingStepEventNames.createProject

            else -> return
        }

        val request = ClevertapRequest(identity, event, completeOnboardingStep)
        clevertapAnalyticsApi.sendAnalytics(listOf(request))
    }
}
