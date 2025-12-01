package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseSingle
import com.anahoret.imagilabsapi.edlink.api.model.School
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.util.*

interface EdLinkSchoolApi {
    fun getSchool(token: String, schoolId: UUID): Either<OperationError, School>
}

@Service
class EdLinkSchoolApiImpl(
    private val edLinkRestTemplate: RestTemplate
) : EdLinkSchoolApi {

    override fun getSchool(token: String, schoolId: UUID): Either<OperationError, School> {
        val request = RequestEntity<Void>
            .get("/v2/graph/schools/$schoolId")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        return edLinkRestTemplate
            .exchange<EdLinkResponseSingle<School>>(request)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_SCHOOL").left()
    }
}
