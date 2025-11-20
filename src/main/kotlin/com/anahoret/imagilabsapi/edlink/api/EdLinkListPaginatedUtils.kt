package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseList
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

object EdLinkListPaginatedUtils {

    inline fun <reified T> list(
        edLinkRestTemplate: RestTemplate,
        urlPattern: String,
        token: String,
        dataFetchErrorMessage: String
    ): Either<OperationError, List<T>> {
        val result = mutableListOf<T>()
        PageableIterator { cursor ->
            val uriTemplate = if (cursor == null) urlPattern
            else $$"$$urlPattern?$cursor=$$cursor"
            val request = RequestEntity<Void>
                .get(uriTemplate)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            edLinkRestTemplate
                .exchange<EdLinkResponseList<T>>(request)
                .takeIf { it.statusCode.is2xxSuccessful }
                ?.body
                ?.right()
                ?: AccessDeniedError(dataFetchErrorMessage).left()
        }.forEach(result::addAll)
        return result.right()
    }
}
