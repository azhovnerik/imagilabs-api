package com.anahoret.imagilabsapi.edlink.web

import arrow.core.Either
import com.anahoret.imagilabsapi.auth.web.RequestAuthenticatorService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.web.ErrorResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.edlink.api.model.UnsupportedUserTypeError
import com.anahoret.imagilabsapi.edlink.domain.EdLinkOAuthCallbackRequest
import com.anahoret.imagilabsapi.edlink.domain.EdLinkOAuthStateService
import com.anahoret.imagilabsapi.edlink.domain.EdLinkOauthCallbackUseCase
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.users.UserType
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/oauth/edlink")
class EdLinkOAuthController(
    private val edLinkOAuthStateService: EdLinkOAuthStateService,
    private val edLinkOauthCallbackUseCase: EdLinkOauthCallbackUseCase,
    private val requestAuthenticatorService: RequestAuthenticatorService,
    private val classroomService: ClassroomService
) {

    @PostMapping("/callback")
    fun getToken(
        @RequestBody request: EdLinkOAuthCallbackRequest,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<*>> {
        return when (val result = edLinkOauthCallbackUseCase.tryAuthenticate(request)) {
            is Either.Left -> handleAuthenticationError(result.value)
            is Either.Right -> authenticate(result.value, response, request)
        }
    }

    @GetMapping("/state")
    fun createState(): ResponseEntity<ResponseDto<String>> {
        return edLinkOAuthStateService.create()
            .toString()
            .let(::SuccessResponseDto)
            .let { ResponseEntity.ok(it) }
    }


    private fun handleAuthenticationError(error: OperationError): ResponseEntity<ResponseDto<*>> {
        val errorMessage = when (error) {
            is AccessDeniedError -> error.message
            is NotFoundError -> error.message
            is UnsupportedUserTypeError -> error.message
            else -> "AUTHENTICATION_FAILED"
        }
        val errorResponse = ErrorResponseDto<String>(HttpStatus.UNAUTHORIZED.value(), errorMessage)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    private fun authenticate(
        userProfile: UserProfile,
        response: HttpServletResponse,
        request: EdLinkOAuthCallbackRequest
    ): ResponseEntity<ResponseDto<*>> {
        return when (userProfile.userType) {
            UserType.TEACHER -> requestAuthenticatorService.authenticateTeacher(
                userProfile.id,
                response,
                request.mobileAppClient
            ).let { ResponseEntity.ok(SuccessResponseDto(it)) }

            UserType.STUDENT -> authenticateStudent(userProfile as StudentProfile, response, request)

            else -> {
                val errorResponse =
                    ErrorResponseDto<String>(HttpStatus.UNAUTHORIZED.value(), UnsupportedUserTypeError.message)
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
            }
        }
    }

    private fun authenticateStudent(
        studentProfile: StudentProfile,
        response: HttpServletResponse,
        request: EdLinkOAuthCallbackRequest
    ): ResponseEntity<ResponseDto<*>> {
        // FIXME: This classroom check logic is duplicated in AuthenticationController
        val classroom = classroomService.getById(studentProfile.classroomId)
            ?: throw InternalAuthenticationServiceException("CLASSROOM_DOES_NOT_EXIST")

        if (classroom.blocked) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseDto<String>(HttpStatus.FORBIDDEN.value(), "SUBSCRIPTION_REQUIRED"))
        }

        return requestAuthenticatorService.authenticateStudent(
            studentProfile.id,
            response,
            request.mobileAppClient,
            studentProfile.classroomId
        ).let { ResponseEntity.ok(SuccessResponseDto(it)) }
    }

}
