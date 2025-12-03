package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.edlink.api.model.Enrollment
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

interface EdLinkEnrollmentApi {
    fun listTeacherEnrollments(token: String, personId: UUID): Either<OperationError, List<Enrollment>>
}

@Service
class EdLinkEnrollmentApiImpl(
    private val edLinkRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) : EdLinkEnrollmentApi {

    override fun listTeacherEnrollments(token: String, personId: UUID): Either<OperationError, List<Enrollment>> {
        // Build $filter parameter for active teacher enrollments
        val filter = edLinkFilter {
            field("person_id") {
                equalsOperator(personId)
            }
            field("role") {
                equalsOperator("teacher")
            }
            field("state") {
                equalsOperator("active")
            }
        }

        val filterJson = objectMapper.writeValueAsString(filter)
        val queryParams = mapOf("\$filter" to filterJson)

        return EdLinkListPaginatedUtils.list(
            edLinkRestTemplate,
            "/v2/graph/enrollments",
            queryParams,
            token,
            "ED_LINK_API_FAILED_TO_GET_ENROLLMENTS"
        )
    }
}
