package com.anahoret.imagilabsapi.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class IndexController {

    @GetMapping("", "/")
    fun index(request: HttpServletRequest): ResponseDto<IndexResponse> {
        val header = request.getHeader("Host")
        val scheme = request.scheme
        return SuccessResponseDto(IndexResponse(scheme, header))
    }

    class IndexResponse(
        private val scheme: String,
        private val host: String
    ) {

        val message: String = "Welcome to ImagiLabs API"
        val documentation: String = "$scheme://$host/swagger-ui.html"
    }

}
