package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.CodeTokenExchangeRequest
import com.anahoret.imagilabsapi.edlink.api.model.CodeTokenExchangeResponse
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseSingle
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

interface EdLinkTokenApi {
    fun exchange(code: String, isLocalhostRedirect: Boolean = false): Either<OperationError, String>
}

@Service
class EdLinkTokenApiImpl(
    private val edLinkRestTemplate: RestTemplate,
    private val edLinkProperties: EdLinkProperties
) : EdLinkTokenApi {

    override fun exchange(code: String, isLocalhostRedirect: Boolean): Either<OperationError, String> {
        val redirectUri = if (isLocalhostRedirect) "http://localhost:3000/auth" else edLinkProperties.redirectUri
        val request = RequestEntity<Void>
            .post("/authentication/token")
            .body(
                CodeTokenExchangeRequest(
                    code,
                    edLinkProperties.clientId,
                    edLinkProperties.clientSecret,
                    redirectUri
                )
            )
        return edLinkRestTemplate
            .exchange(
                request,
                object : ParameterizedTypeReference<EdLinkResponseSingle<CodeTokenExchangeResponse>>() {})
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.accessToken
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_MY_PROFILE").left()
    }

}

