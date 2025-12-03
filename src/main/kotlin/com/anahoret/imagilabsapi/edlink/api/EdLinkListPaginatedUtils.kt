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
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

object EdLinkListPaginatedUtils {

    inline fun <reified T> list(
        edLinkRestTemplate: RestTemplate,
        urlPattern: String,
        token: String,
        dataFetchErrorMessage: String
    ): Either<OperationError, List<T>> {
        return list(edLinkRestTemplate, urlPattern, emptyMap(), token, dataFetchErrorMessage)
    }

    inline fun <reified T> list(
        edLinkRestTemplate: RestTemplate,
        urlPattern: String,
        queryParams: Map<String, String>,
        token: String,
        dataFetchErrorMessage: String
    ): Either<OperationError, List<T>> {
        val result = mutableListOf<T>()
        var cursor: String? = null

        // Process pages explicitly with proper error handling
        do {
            val uri = buildUri(urlPattern, queryParams, cursor)
            val request = RequestEntity<Void>
                .get(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()

            val response = edLinkRestTemplate
                .exchange<EdLinkResponseList<T>>(request)
                .takeIf { it.statusCode.is2xxSuccessful }
                ?.body
                ?: return AccessDeniedError(dataFetchErrorMessage).left()

            result.addAll(response.data)
            cursor = response.cursor
        } while (cursor != null && result.isNotEmpty())

        return result.right()
    }

    fun buildUri(urlPattern: String, queryParams: Map<String, String>, cursor: String?): URI {
        val builder = UriComponentsBuilder.fromUriString(urlPattern)

        queryParams.forEach { (key, value) ->
            builder.queryParam(key, value)
        }

        if (cursor != null) {
            builder.queryParam("\$cursor", cursor)
        }

        return builder.build().toUri()
    }
}