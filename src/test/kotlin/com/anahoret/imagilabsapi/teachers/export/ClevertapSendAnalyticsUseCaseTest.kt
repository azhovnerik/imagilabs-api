package com.anahoret.imagilabsapi.teachers.export

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import com.anahoret.imagilabsapi.teachers.export.clevertap.api.ClevertapAnalyticsApi
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapRequest
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapSendAnalyticsUseCaseImpl
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Clevertap send analytics use case")
class ClevertapSendAnalyticsUseCaseTest {

    private val clevertapAnalyticsApi = mockk<ClevertapAnalyticsApi>()
    private val clevertapSendAnalyticsUseCase = ClevertapSendAnalyticsUseCaseImpl(clevertapAnalyticsApi)

    private val eventType = "event"
    private val completeOnboardingStepEventName = "complete_onboarding_step"

    @Test
    fun `should send teachers expired subscription event when subscription is expired`() {
        val teacherId = UUID.randomUUID()
        val teachersIds = listOf(teacherId)
        val requests = listOf(ClevertapRequest(teacherId.toString(), eventType, "s_subscription_expired"))

        every { clevertapAnalyticsApi.sendAnalytics(requests) } returns mockk()

        clevertapSendAnalyticsUseCase.sendTeachersExpiredSubscriptionEvent(teachersIds)

        verify { clevertapAnalyticsApi.sendAnalytics(requests) }
    }

    @Test
    fun `should send complete onboarding step event when teacher complete 'create or join classroom' step`() {
        val step = TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM
        val requests = listOf(
            ClevertapRequest("createOrJoinClassroom", eventType, completeOnboardingStepEventName)
        )

        every { clevertapAnalyticsApi.sendAnalytics(requests) } returns mockk()

        clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(step)

        verify { clevertapAnalyticsApi.sendAnalytics(requests) }
    }

    @Test
    fun `should send complete onboarding step event when teacher complete 'share student credentials' step`() {
        val step = TeacherCheckListStep.SHARE_STUDENT_ACCESS_CODE
        val requests = listOf(
            ClevertapRequest("shareStudentCredentials", eventType, completeOnboardingStepEventName)
        )

        every { clevertapAnalyticsApi.sendAnalytics(requests) } returns mockk()

        clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(step)

        verify { clevertapAnalyticsApi.sendAnalytics(requests) }
    }

    @Test
    fun `should send complete onboarding step event when teacher complete 'explore first lesson' step`() {
        val step = TeacherCheckListStep.EXPLORE_YOUR_FIRST_LESSON
        val requests = listOf(
            ClevertapRequest("exploreLesson", eventType, completeOnboardingStepEventName)
        )

        every { clevertapAnalyticsApi.sendAnalytics(requests) } returns mockk()

        clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(step)

        verify { clevertapAnalyticsApi.sendAnalytics(requests) }
    }

    @Test
    fun `should send complete onboarding step event when teacher complete 'create first project' step`() {
        val step = TeacherCheckListStep.CREATE_YOUR_FIRST_PROJECT
        val requests = listOf(
            ClevertapRequest("createProject", eventType, completeOnboardingStepEventName)
        )

        every { clevertapAnalyticsApi.sendAnalytics(requests) } returns mockk()

        clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(step)

        verify { clevertapAnalyticsApi.sendAnalytics(requests) }
    }

    @Test
    fun `should send complete onboarding step event when teacher complete 'check facebook group' step`() {
        val step = TeacherCheckListStep.CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP
        val requests = listOf(
            ClevertapRequest("checkFacebookGroup", eventType, completeOnboardingStepEventName)
        )

        every { clevertapAnalyticsApi.sendAnalytics(requests) } returns mockk()

        clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(step)

        verify { clevertapAnalyticsApi.sendAnalytics(requests) }
    }

    @Test
    fun `should not send complete onboarding step event when have been showed congratulation dialog`() {
        val step = TeacherCheckListStep.CONGRATULATION_DIALOG_SHOWN

        clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(step)

        verify { clevertapAnalyticsApi.sendAnalytics(any()) wasNot called }
    }
}
