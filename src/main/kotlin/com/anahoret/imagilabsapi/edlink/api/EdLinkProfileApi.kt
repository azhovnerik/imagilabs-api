package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseSingle
import com.anahoret.imagilabsapi.edlink.api.model.Person
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

interface EdLinkProfileApi {
    fun myProfile(token: String): Either<OperationError, Person>
}

@Service
class EdLinkProfileApiImpl(
    private val edLinkRestTemplate: RestTemplate
) : EdLinkProfileApi {

    override fun myProfile(token: String): Either<OperationError, Person> {
        val request = RequestEntity<Void>
            .get("/v2/my/profile")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        return edLinkRestTemplate
            .exchange(request, object : ParameterizedTypeReference<EdLinkResponseSingle<Person>>() {})
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_MY_PROFILE").left()
    }

}
