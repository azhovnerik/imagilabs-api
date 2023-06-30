package com.anahoret.imagilabsapi.teachers.export.clevertap.api

import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapRequest
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

interface ClevertapAnalyticsApi {

    fun sendAnalytics(requests: List<ClevertapRequest>): ClevertapResponse
}

@Service
@Profile("prod", "stage")
class ClevertapAnalyticsApiImpl(
    private val clevertapRestTemplate: RestTemplate
): ClevertapAnalyticsApi {

    override fun sendAnalytics(requests: List<ClevertapRequest>): ClevertapResponse {
        return clevertapRestTemplate.postForObject(
            url = "/1/upload",
            request = ClevertapWrapper(requests)
        )
    }
}

@Suppress("unused")
private class ClevertapWrapper(
    val d: List<ClevertapRequest>
)
