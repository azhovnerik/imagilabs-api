package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseList
import com.anahoret.imagilabsapi.edlink.api.model.Enrollment
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

interface EdLinkEnrollmentApi {
    fun getTeacherEnrollments(token: String, personId: UUID): Either<OperationError, List<Enrollment>>
}

@Service
class EdLinkEnrollmentApiImpl(
    private val edLinkRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) : EdLinkEnrollmentApi {

    override fun getTeacherEnrollments(token: String, personId: UUID): Either<OperationError, List<Enrollment>> {
        // Build $filter parameter for active teacher enrollments
        val filter = mapOf(
            "person_id" to listOf(
                mapOf(
                    "operator" to "equals",
                    "value" to personId.toString()
                )
            ),
            "role" to listOf(
                mapOf(
                    "operator" to "equals",
                    "value" to "teacher"
                )
            ),
            "state" to listOf(
                mapOf(
                    "operator" to "equals",
                    "value" to "active"
                )
            )
        )

        val filterJson = objectMapper.writeValueAsString(filter)

        val uri = UriComponentsBuilder.fromUriString("/v2/graph/enrollments")
            .queryParam("\$filter", filterJson)
            .build()
            .toUri()

        val request = RequestEntity<Void>
            .get(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()

        return edLinkRestTemplate
            .exchange<EdLinkResponseList<Enrollment>>(request)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_ENROLLMENTS").left()
    }
}