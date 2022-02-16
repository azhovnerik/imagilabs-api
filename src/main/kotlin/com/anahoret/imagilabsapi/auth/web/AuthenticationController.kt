package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.web.ResponseDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
class AuthenticationController(
    private val requestAuthenticatorService: RequestAuthenticatorService,
) {

    @PostMapping("/api/auth/logout")
    fun logout(response: HttpServletResponse): ResponseDto<Void> {
        requestAuthenticatorService.logout(response)
        return EmptySuccessResponseDto
    }

}
