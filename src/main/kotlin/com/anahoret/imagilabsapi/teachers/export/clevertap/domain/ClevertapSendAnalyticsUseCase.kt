package com.anahoret.imagilabsapi.teachers.export.clevertap.domain

import com.anahoret.imagilabsapi.teachers.export.clevertap.api.ClevertapAnalyticsApi
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

interface ClevertapSendAnalyticsUseCase {

    fun sendTeachersExpiredSubscriptionEvent(teachersIds: List<UUID>)
}

@Service
@Profile("prod", "stage")
class ClevertapSendAnalyticsUseCaseImpl(
    private val clevertapAnalyticsApi: ClevertapAnalyticsApi
) : ClevertapSendAnalyticsUseCase {

    override fun sendTeachersExpiredSubscriptionEvent(teachersIds: List<UUID>) {
        teachersIds
            .map {
                ClevertapRequest(
                    it.toString(),
                    ClevertapTypes.event,
                    ClevertapEvents.teacherSubscriptionExpired
                )
            }
            .apply { clevertapAnalyticsApi.sendAnalytics(this) }
    }
}
