package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.auth.domain.ImagiLabsAuthenticationToken
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ErrorResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.students.domain.StudentLoginRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherLoginRequest
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InternalAuthenticationServiceException
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
    fun teacherLogin(
        @RequestBody teacherLoginRequest: TeacherLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<TeacherAuthenticationSuccess?>> {
        val authToken = ImagiLabsAuthenticationToken(
            teacherLoginRequest.email.lowercase(),
            UserType.TEACHER,
            teacherLoginRequest.password
        )
        return tryAuthenticate(authToken, teacherLoginRequest.mobileAppClient, response)
    }

    @PostMapping("/api/auth/student")
    fun studentLogin(
        @RequestBody studentLoginRequest: StudentLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<StudentAuthenticationSuccess?>> {
        val authToken = ImagiLabsAuthenticationToken(
            studentLoginRequest.username,
            UserType.STUDENT,
            studentLoginRequest
        )
        return tryAuthenticate(authToken, studentLoginRequest.mobileAppClient, response)
    }

    private inline fun <reified T : AuthenticationSuccess> tryAuthenticate(
        imagiLabsAuthenticationToken: ImagiLabsAuthenticationToken,
        mobileAppClient: Boolean,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<T?>> {
        return try {
            val authentication =
                authenticationManager.authenticate(imagiLabsAuthenticationToken) as ImagiLabsAuthenticationToken
            val principalId = authentication.getPrincipalId()
                ?: throw InternalAuthenticationServiceException("PRINCIPAL_ID_IS_NULL")
            val authenticationResponse = requestAuthenticatorService.authenticate(
                principalId,
                imagiLabsAuthenticationToken.userType,
                response,
                mobileAppClient
            ) as T
            ResponseEntity.ok(SuccessResponseDto(authenticationResponse))
        } catch (e: DisabledException) {
            val errorResponse = ErrorResponseDto<T?>(HttpStatus.UNAUTHORIZED.value(), "ACCOUNT_NOT_ACTIVE")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
        } catch (e: BadCredentialsException) {
            val errorResponse = ErrorResponseDto<T?>(HttpStatus.UNAUTHORIZED.value(), "WRONG_CREDENTIALS")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
        } catch (e: AuthenticationException) {
            val errorResponse = ErrorResponseDto<T?>(HttpStatus.UNAUTHORIZED.value(), "AUTHENTICATION_FAILED")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
        }
    }

}
