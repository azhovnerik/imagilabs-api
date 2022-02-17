package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.auth.domain.ImagiLabsAuthentication
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ErrorResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.teachers.domain.TeacherLoginRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
class AuthenticationController(
    private val authenticationManager: AuthenticationManager,
    private val requestAuthenticatorService: RequestAuthenticatorService,
) {

    @PostMapping("/api/auth/logout")
    fun logout(response: HttpServletResponse): ResponseDto<Void> {
        requestAuthenticatorService.logout(response)
        return EmptySuccessResponseDto
    }

    @PostMapping("/api/auth/teacher")
    fun teacherLogIn(
        @RequestBody teacherLoginRequest: TeacherLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<*>> {
        return tryAuthenticate {
            val usernameLowerCased = teacherLoginRequest.email.lowercase()
            val password = teacherLoginRequest.password
            val authentication =
                authenticationManager.authenticate(
                    ImagiLabsAuthentication(
                        usernameLowerCased,
                        UserType.TEACHER,
                        password
                    )
                )
            val principal = authentication.principal as TeacherProfile
            val authenticationResponse = requestAuthenticatorService.authenticate(
                principal.id,
                UserType.TEACHER,
                response,
                teacherLoginRequest.mobileAppClient
            )
            ResponseEntity.ok(SuccessResponseDto(authenticationResponse))
        }
    }

    private fun tryAuthenticate(authenticate: () -> ResponseEntity<ResponseDto<*>>): ResponseEntity<ResponseDto<*>> {
        return try {
            authenticate()
        } catch (e: DisabledException) {
            val errorResponse = ErrorResponseDto<Any>(HttpStatus.UNAUTHORIZED.value(), "ACCOUNT_NOT_ACTIVE")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
        } catch (e: BadCredentialsException) {
            val errorResponse = ErrorResponseDto<Any>(HttpStatus.UNAUTHORIZED.value(), "WRONG_CREDENTIALS")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
        } catch (e: AuthenticationException) {
            val errorResponse = ErrorResponseDto<Any>(HttpStatus.UNAUTHORIZED.value(), "AUTHENTICATION_FAILED")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
        }
    }

}
