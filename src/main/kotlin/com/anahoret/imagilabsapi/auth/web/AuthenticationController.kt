package com.anahoret.imagilabsapi.auth.web

import arrow.core.Either
import com.anahoret.imagilabsapi.auth.domain.ImagiLabsAuthenticationToken
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.web.*
import com.anahoret.imagilabsapi.students.domain.StudentLoginRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherForgotPasswordRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherLoginRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherResetPasswordRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherResetPasswordUseCase
import com.anahoret.imagilabsapi.users.UserType
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletResponse
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
import java.util.*

@RestController
class AuthenticationController(
    private val authenticationManager: AuthenticationManager,
    private val requestAuthenticatorService: RequestAuthenticatorService,
    private val classroomService: ClassroomService,
    private val teacherResetPasswordUseCase: TeacherResetPasswordUseCase
) {

    @PostMapping("/api/auth/logout")
    fun logout(response: HttpServletResponse): ResponseDto<Void> {
        requestAuthenticatorService.logout(response)
        return EmptySuccessResponseDto
    }

    // Workaround for Swagger documentation. Needed for generic endpoint response docs.
    class AdminAuthSuccessResponseDto : SuccessResponseDto<AdminAuthenticationSuccess>(null, emptyList())
    class TeacherAuthSuccessResponseDto : SuccessResponseDto<TeacherAuthenticationSuccess>(null, emptyList())

    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "OK",
            content = [Content(
                array = ArraySchema(
                    schema = Schema(
                        type = "object",
                        oneOf = [AdminAuthSuccessResponseDto::class, TeacherAuthSuccessResponseDto::class]
                    )
                )
            )]
        )
    )

    @PostMapping("/api/auth/teacher")
    fun teacherLogin(
        @RequestBody teacherLoginRequest: TeacherLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        return tryAuthenticateAdmin(teacherLoginRequest, response)
            .takeIf { it.statusCode.is2xxSuccessful }
            ?: tryAuthenticateTeacher(teacherLoginRequest, response)
    }

    @PostMapping("/api/auth/teacher/forgot-password")
    fun teacherForgotPassword(
        @RequestBody teacherForgotPasswordRequest: TeacherForgotPasswordRequest,
    ): ResponseEntity<ResponseDto<Void>> {
        teacherResetPasswordUseCase.sendVerificationCode(teacherForgotPasswordRequest.email)
        return ResponseEntity.ok(EmptySuccessResponseDto)
    }

    @PostMapping("/api/auth/teacher/reset-password")
    fun teacherResetPassword(
        @RequestBody teacherResetPasswordRequest: TeacherResetPasswordRequest,
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = teacherResetPasswordUseCase.resetPassword(teacherResetPasswordRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

    @PostMapping("/api/auth/student")
    fun studentLogin(
        @RequestBody studentLoginRequest: StudentLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<StudentAuthenticationSuccess?>> {
        // FIXME: This classroom check logic is duplicated in EdLinkOAuthController
        val classroom = classroomService.getByAccessCode(studentLoginRequest.classroomAccessCode)
            ?: throw InternalAuthenticationServiceException("CLASSROOM_DOES_NOT_EXIST")

        if (classroom.blocked) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseDto(HttpStatus.FORBIDDEN.value(), "SUBSCRIPTION_REQUIRED"))
        }

        val authToken = ImagiLabsAuthenticationToken(
            studentLoginRequest.username,
            UserType.STUDENT,
            studentLoginRequest
        )
        return tryAuthenticate(authToken) { principalId ->
            val authenticationResponse = requestAuthenticatorService.authenticateStudent(
                principalId,
                response,
                studentLoginRequest.mobileAppClient,
                classroom.id
            )
            SuccessResponseDto(authenticationResponse)
        }
    }

    private fun tryAuthenticateTeacher(
        teacherLoginRequest: TeacherLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<TeacherAuthenticationSuccess?>> {
        val authToken = ImagiLabsAuthenticationToken(
            teacherLoginRequest.email.lowercase(),
            UserType.TEACHER,
            teacherLoginRequest.password
        )
        return tryAuthenticate(authToken) { principalId ->
            val authenticationResponse = requestAuthenticatorService.authenticateTeacher(
                principalId,
                response,
                teacherLoginRequest.mobileAppClient
            )
            SuccessResponseDto(authenticationResponse)
        }
    }

    private fun tryAuthenticateAdmin(
        teacherLoginRequest: TeacherLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<AdminAuthenticationSuccess?>> {
        val authToken = ImagiLabsAuthenticationToken(
            teacherLoginRequest.email.lowercase(),
            UserType.ADMIN,
            teacherLoginRequest.password
        )
        return tryAuthenticate(authToken) { principalId ->
            val authenticationResponse = requestAuthenticatorService.authenticateAdmin(
                principalId,
                response,
                teacherLoginRequest.mobileAppClient
            )
            SuccessResponseDto(authenticationResponse)
        }
    }

    private fun <T> tryAuthenticate(
        authToken: ImagiLabsAuthenticationToken,
        f: (UUID) -> ResponseDto<T?>
    ): ResponseEntity<ResponseDto<T?>> {
        fun unauthorized(message: String): ResponseEntity<ResponseDto<T?>> {
            val errorResponse = ErrorResponseDto<T?>(HttpStatus.UNAUTHORIZED.value(), message)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
        }

        return try {
            val authentication =
                authenticationManager.authenticate(authToken) as ImagiLabsAuthenticationToken
            val principalId = authentication.getPrincipalId()
                ?: throw InternalAuthenticationServiceException("PRINCIPAL_ID_IS_NULL")
            ResponseEntity.ok(f(principalId))
        } catch (e: DisabledException) {
            unauthorized("ACCOUNT_NOT_ACTIVE")
        } catch (e: BadCredentialsException) {
            unauthorized("WRONG_CREDENTIALS")
        } catch (e: AuthenticationException) {
            unauthorized("AUTHENTICATION_FAILED")
        }
    }

}
