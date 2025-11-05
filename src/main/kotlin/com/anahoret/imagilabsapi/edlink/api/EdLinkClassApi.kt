package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkClass
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseList
import com.anahoret.imagilabsapi.edlink.api.model.Person
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.util.*

interface EdLinkClassApi {
    fun listClasses(token: String): Either<OperationError, List<EdLinkClass>>
    fun listTeachers(token: String, classId: UUID): Either<OperationError, List<Person>>
    fun listStudents(token: String, classId: UUID): Either<OperationError, List<Person>>
}

@Service
class EdLinkClassApiImpl(
    private val edLinkRestTemplate: RestTemplate
) : EdLinkClassApi {

    override fun listClasses(token: String): Either<OperationError, List<EdLinkClass>> {
        val request = RequestEntity<Void>
            .get("/v2/graph/classes")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        return edLinkRestTemplate
            .exchange<EdLinkResponseList<EdLinkClass>>(request)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_LIST_CLASSES").left()
    }

    override fun listTeachers(token: String, classId: UUID): Either<OperationError, List<Person>> {
        val request = RequestEntity<Void>
            .get("/v2/graph/classes/$classId/teachers")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        return edLinkRestTemplate
            .exchange<EdLinkResponseList<Person>>(request)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_LIST_TEACHERS_FOR_CLASS").left()
    }

    override fun listStudents(token: String, classId: UUID): Either<OperationError, List<Person>> {
        val request = RequestEntity<Void>
            .get("/v2/graph/classes/$classId/students")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        return edLinkRestTemplate
            .exchange<EdLinkResponseList<Person>>(request)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_LIST_STUDENTS_FOR_CLASS").left()
    }

}
