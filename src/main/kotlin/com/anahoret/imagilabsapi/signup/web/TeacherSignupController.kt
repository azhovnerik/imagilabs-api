package com.anahoret.imagilabsapi.signup.web

import arrow.core.Either
import com.anahoret.imagilabsapi.auth.web.RequestAuthenticatorService
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.common.web.ErrorResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseErrorMessageDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.signup.domain.TeacherSignUpUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherSignupRequest
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
class TeacherSignupController(
    private val requestAuthenticatorService: RequestAuthenticatorService,
    private val teacherSignUpUseCase: TeacherSignUpUseCase
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
