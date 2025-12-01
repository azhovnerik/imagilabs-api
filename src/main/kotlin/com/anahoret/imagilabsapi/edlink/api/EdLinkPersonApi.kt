package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseSingle
import com.anahoret.imagilabsapi.edlink.api.model.PersonDetails
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.util.*

interface EdLinkPersonApi {
    fun getPerson(token: String, personId: UUID): Either<OperationError, PersonDetails>
}

@Service
class EdLinkPersonApiImpl(
    private val edLinkRestTemplate: RestTemplate
) : EdLinkPersonApi {

    override fun getPerson(token: String, personId: UUID): Either<OperationError, PersonDetails> {
        val request = RequestEntity<Void>
            .get("/v2/graph/people/$personId")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        return edLinkRestTemplate
            .exchange<EdLinkResponseSingle<PersonDetails>>(request)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_PERSON").left()
    }

}