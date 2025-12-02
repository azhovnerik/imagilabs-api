package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkClass
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseList
import com.anahoret.imagilabsapi.edlink.api.model.Person
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

interface EdLinkClassApi {
    fun listClasses(token: String): Either<OperationError, List<EdLinkClass>>
    fun getClasses(token: String, classIds: List<UUID>): Either<OperationError, List<EdLinkClass>>
    fun listTeachers(token: String, classId: UUID): Either<OperationError, List<Person>>
    fun listStudents(token: String, classId: UUID): Either<OperationError, List<Person>>
}

@Service
class EdLinkClassApiImpl(
    private val edLinkRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) : EdLinkClassApi {

    override fun listClasses(token: String): Either<OperationError, List<EdLinkClass>> {
        return EdLinkListPaginatedUtils.list(
            edLinkRestTemplate,
            "/v2/graph/classes",
            token,
            "ED_LINK_API_FAILED_TO_LIST_CLASSES"
        )
    }

    override fun getClasses(token: String, classIds: List<UUID>): Either<OperationError, List<EdLinkClass>> {
        if (classIds.isEmpty()) {
            return emptyList<EdLinkClass>().right()
        }

        // Build $filter parameter for class IDs
        val filter = mapOf(
            "id" to listOf(
                mapOf(
                    "operator" to "in",
                    "value" to classIds.joinToString(",")
                )
            )
        )

        val filterJson = objectMapper.writeValueAsString(filter)

        val uri = UriComponentsBuilder.fromUriString("/v2/graph/classes")
            .queryParam("\$filter", filterJson)
            .build()
            .toUri()

        val request = RequestEntity<Void>
            .get(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()

        return edLinkRestTemplate
            .exchange<EdLinkResponseList<EdLinkClass>>(request)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_CLASSES").left()
    }

    override fun listTeachers(token: String, classId: UUID): Either<OperationError, List<Person>> {
        return EdLinkListPaginatedUtils.list(
            edLinkRestTemplate, "/v2/graph/classes/$classId/teachers",
            token,
            "ED_LINK_API_FAILED_TO_LIST_TEACHERS_FOR_CLASS"
        )
    }

    override fun listStudents(token: String, classId: UUID): Either<OperationError, List<Person>> {
        return EdLinkListPaginatedUtils.list(
            edLinkRestTemplate, "/v2/graph/classes/$classId/students",
            token,
            "ED_LINK_API_FAILED_TO_LIST_STUDENTS_FOR_CLASS"
        )
    }

}
