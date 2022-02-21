package com.anahoret.imagilabsapi.signup.web

import arrow.core.Either
import com.anahoret.imagilabsapi.auth.web.RequestAuthenticatorService
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.common.web.*
import com.anahoret.imagilabsapi.signup.domain.TeacherEmailVerificationUseCase
import com.anahoret.imagilabsapi.signup.domain.TeacherSignUpUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherEmailVerificationRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherSignupRequest
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
class TeacherSignupController(
    private val requestAuthenticatorService: RequestAuthenticatorService,
    private val teacherSignUpUseCase: TeacherSignUpUseCase,
    private val teacherEmailVerificationUseCase: TeacherEmailVerificationUseCase
) {

    @PostMapping("/api/sign-up/teacher")
    fun signUp(
        @RequestBody signUpRequest: TeacherSignupRequest,
        response: HttpServletResponse
    ): ResponseEntity<ResponseDto<*>> {
        return when (val result = teacherSignUpUseCase.signUp(signUpRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> authenticateTeacher(result.value, response, signUpRequest.mobileAppClient)
        }
    }

    @Secured("ROLE_TEACHER_EMAIL_NOT_VERIFIED")
    @PostMapping("/api/sign-up/teacher/email-verification")
    fun emailVerification(
        @RequestBody emailVerificationRequest: TeacherEmailVerificationRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<*>> {
        return if (teacherEmailVerificationUseCase.verify(teacherProfile.id, emailVerificationRequest.code)) {
            ResponseEntity.ok(EmptySuccessResponseDto)
        } else {
            createWrongVerificationCodeResponse()
        }
    }

    private fun createWrongVerificationCodeResponse(): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity.badRequest()
            .body(ErrorResponseDto<Void>(HttpStatus.BAD_REQUEST.value(), "VERIFICATION_CODE_DOES_NOT_MATCH"))
    }

    private fun mapErrors(validationErrors: List<ValidationError>): ResponseEntity<ResponseDto<*>> {
        val responseErrors = validationErrors.map {
            ResponseErrorMessageDto(HttpStatus.BAD_REQUEST.value(), it.message)
        }
        return ResponseEntity.badRequest().body(ErrorResponseDto<Void>(responseErrors))
    }

    private fun authenticateTeacher(
        teacherProfile: TeacherProfile,
        response: HttpServletResponse,
        mobileAppClient: Boolean
    ): ResponseEntity<ResponseDto<*>> {
        val authenticationResponse = requestAuthenticatorService.authenticate(
            teacherProfile.id,
            UserType.TEACHER,
            response,
            mobileAppClient
        )
        return ResponseEntity.ok(SuccessResponseDto(authenticationResponse))
    }

}
