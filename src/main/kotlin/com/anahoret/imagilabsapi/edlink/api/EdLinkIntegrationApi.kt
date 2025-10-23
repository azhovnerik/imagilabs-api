package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseSingle
import com.anahoret.imagilabsapi.edlink.api.model.Integration
import com.anahoret.imagilabsapi.edlink.api.model.MyIntegration
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

interface EdLinkIntegrationApi {
    fun myIntegration(token: String): Either<OperationError, MyIntegration>
    fun getIntegration(id: UUID): Either<OperationError, Integration>
}

@Service
class EdLinkIntegrationApiImpl(
    private val edLinkRestTemplate: RestTemplate,
    private val edLinkProperties: EdLinkProperties
) : EdLinkIntegrationApi {

    override fun myIntegration(token: String): Either<OperationError, MyIntegration> {
        val request = RequestEntity<Void>
            .get("/v2/my/integration")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        return edLinkRestTemplate
            .exchange(request, object : ParameterizedTypeReference<EdLinkResponseSingle<MyIntegration>>() {})
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_MY_INTEGRATION").left()
    }

    override fun getIntegration(id: UUID): Either<OperationError, Integration> {
        val request = RequestEntity<Void>
            .get("/v1/integrations/$id")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${edLinkProperties.clientSecret}")
            .build()
        return edLinkRestTemplate
            .exchange(request, object : ParameterizedTypeReference<EdLinkResponseSingle<Integration>>() {})
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_INTEGRATION").left()
    }

}
