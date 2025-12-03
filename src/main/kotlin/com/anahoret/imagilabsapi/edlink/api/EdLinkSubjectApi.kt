package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.edlink.api.model.Subject
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

interface EdLinkSubjectApi {
    fun listSubjects(token: String, subjectIds: List<UUID>): Either<OperationError, List<Subject>>
}

@Service
class EdLinkSubjectApiImpl(
    private val edLinkRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) : EdLinkSubjectApi {

    override fun listSubjects(token: String, subjectIds: List<UUID>): Either<OperationError, List<Subject>> {
        if (subjectIds.isEmpty()) {
            return emptyList<Subject>().right()
        }

        // Build $filter parameter for subject IDs
        val filter = edLinkFilter {
            field("id") {
                inOperator(subjectIds)
            }
        }

        val filterJson = objectMapper.writeValueAsString(filter)
        val queryParams = mapOf("\$filter" to filterJson)

        return EdLinkListPaginatedUtils.list(
            edLinkRestTemplate,
            "/v2/graph/subjects",
            queryParams,
            token,
            "ED_LINK_API_FAILED_TO_GET_SUBJECTS"
        )
    }
}
