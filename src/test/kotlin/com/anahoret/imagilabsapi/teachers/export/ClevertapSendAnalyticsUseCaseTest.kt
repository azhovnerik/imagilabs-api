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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.*

@DisplayName("Clevertap send analytics use case")
class ClevertapSendAnalyticsUseCaseTest {

    private val clevertapAnalyticsApi = mockk<ClevertapAnalyticsApi>()
    private val clevertapSendAnalyticsUseCase = ClevertapSendAnalyticsUseCaseImpl(clevertapAnalyticsApi)

    private val eventType = "event"
    private val completeOnboardingStepEventName = "h_complete_onboarding_step"

    @Nested
    @DisplayName("When send teachers expired subscription event")
    inner class SendTeachersExpiredSubscriptionEvent {

        @Test
        fun `should send teachers expired subscription event when subscription is expired`() {
            val teacherId = UUID.randomUUID()
            val teachersIds = listOf(teacherId)
            val requests = listOf(ClevertapRequest(teacherId.toString(), eventType, "s_subscription_expired"))

            every { clevertapAnalyticsApi.sendAnalytics(requests) } returns mockk()

            clevertapSendAnalyticsUseCase.sendTeachersExpiredSubscriptionEvent(teachersIds)

            verify { clevertapAnalyticsApi.sendAnalytics(requests) }
        }
    }

    @Nested
    @DisplayName("When send complete onboarding step event")
    inner class SendCompleteOnboardingStepEvent {

        private val teacherId = UUID.randomUUID()

        @ParameterizedTest
        @EnumSource(
            TeacherCheckListStep::class,
            names = ["CONGRATULATION_DIALOG_SHOWN"],
            mode = EnumSource.Mode.EXCLUDE
        )
        fun `should send complete onboarding step event when teacher complete all events except 'CONGRATULATION_DIALOG_SHOWN'`(
            step: TeacherCheckListStep
        ) {
            val requests = listOf(
                ClevertapRequest(
                    identity = teacherId.toString(),
                    type = eventType,
                    evtName = completeOnboardingStepEventName,
                    evtData = step.clevertapName
                )
            )

            every { clevertapAnalyticsApi.sendAnalytics(requests) } returns mockk()

            clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(teacherId, step)

            verify { clevertapAnalyticsApi.sendAnalytics(requests) }
        }

        @Test
        fun `should not send complete onboarding step event when have been showed congratulation dialog`() {
            val step = TeacherCheckListStep.CONGRATULATION_DIALOG_SHOWN

            clevertapSendAnalyticsUseCase.sendCompleteOnboardingStepEvent(teacherId, step)

            verify { clevertapAnalyticsApi.sendAnalytics(any()) wasNot called }
        }
    }

}
