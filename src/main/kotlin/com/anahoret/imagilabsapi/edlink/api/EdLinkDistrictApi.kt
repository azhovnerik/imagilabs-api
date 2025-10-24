package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.model.District
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseSingle
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

interface EdLinkDistrictApi {
    fun myDistrict(token: String, districtId: UUID): Either<OperationError, District>
}

@Service
class EdLinkDistrictApiImpl(
    private val edLinkRestTemplate: RestTemplate
) : EdLinkDistrictApi {

    override fun myDistrict(token: String, districtId: UUID): Either<OperationError, District> {
        val request = RequestEntity<Void>
            .get("/v2/my/districts/$districtId")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        return edLinkRestTemplate
            .exchange(request, object : ParameterizedTypeReference<EdLinkResponseSingle<District>>() {})
            .takeIf { it.statusCode.is2xxSuccessful }
            ?.body
            ?.data
            ?.right()
            ?: AccessDeniedError("ED_LINK_API_FAILED_TO_GET_MY_DISTRICT").left()
    }

}
