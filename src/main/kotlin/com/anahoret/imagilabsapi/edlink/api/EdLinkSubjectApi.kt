package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseList
import com.anahoret.imagilabsapi.edlink.api.model.Subject
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

interface EdLinkSubjectApi {
    fun getSubjects(token: String, subjectIds: List<UUID>): Either<OperationError, List<Subject>>
}

@Service
class EdLinkSubjectApiImpl(
    private val edLinkRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) : EdLinkSubjectApi {

    override fun getSubjects(token: String, subjectIds: List<UUID>): Either<OperationError, List<Subject>> {
        if (subjectIds.isEmpty()) {
            return emptyList<Subject>().right()
        }

        // Build $filter parameter for subject IDs
        val filter = mapOf(
            "id" to listOf(
                mapOf(
                    "operator" to "in",
                    "value" to subjectIds.joinToString(",")
                )
            )
        )

        val filterJson = objectMapper.writeValueAsString(filter)

        val uri = UriComponentsBuilder.fromUriString("/v2/graph/subjects")
            .queryParam("\$filter", filterJson)
            .build()
            .toUri()

        val request = RequestEntity<Void>
            .get(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()

        return edLinkRestTemplate
            .exchange<EdLinkResponseList<Subject>>(request)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_SUBJECTS").left()
    }
}