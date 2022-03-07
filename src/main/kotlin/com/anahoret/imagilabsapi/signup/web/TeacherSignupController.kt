package com.anahoret.imagilabsapi.signup.web

import arrow.core.Either
import com.anahoret.imagilabsapi.auth.web.AuthenticationSuccess
import com.anahoret.imagilabsapi.auth.web.RequestAuthenticatorService
import com.anahoret.imagilabsapi.common.web.ErrorResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.toBadRequestResponse
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.signup.domain.TeacherEmailVerificationUseCase
import com.anahoret.imagilabsapi.signup.domain.TeacherSignUpUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherEmailVerificationRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherSignupRequest
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
    ): ResponseEntity<ResponseDto<AuthenticationSuccess?>> {
        return when (val result = teacherSignUpUseCase.signUp(signUpRequest)) {
            is Either.Left -> result.value.toBadRequestResponse()
            is Either.Right -> authenticateTeacher(result.value, response, signUpRequest.mobileAppClient)
        }
    }

    @Secured(UserRole.teacherEmailNotVerified)
    @PostMapping("/api/sign-up/teacher/email-verification")
    fun emailVerification(
        @RequestBody emailVerificationRequest: TeacherEmailVerificationRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<TeacherProfile?>> {
        val verifiedTeacher = teacherEmailVerificationUseCase.verify(teacherProfile.id, emailVerificationRequest.code)
        return if (verifiedTeacher != null) {
            ResponseEntity.ok(SuccessResponseDto(verifiedTeacher))
        } else {
            createWrongVerificationCodeResponse()
        }
    }

    private fun createWrongVerificationCodeResponse(): ResponseEntity<ResponseDto<TeacherProfile?>> {
        return ResponseEntity.badRequest()
            .body(ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), "VERIFICATION_CODE_DOES_NOT_MATCH"))
    }

    private fun authenticateTeacher(
        teacherProfile: TeacherProfile,
        response: HttpServletResponse,
        mobileAppClient: Boolean
    ): ResponseEntity<ResponseDto<AuthenticationSuccess?>> {
        val authenticationResponse = requestAuthenticatorService.authenticateTeacher(
            teacherProfile.id,
            response,
            mobileAppClient
        )
        return ResponseEntity.ok(SuccessResponseDto(authenticationResponse))
    }

}
