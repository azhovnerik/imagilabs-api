package com.anahoret.imagilabsapi.signup.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherSignupRequest
import org.springframework.stereotype.Service

interface TeacherSignUpUseCase {

    fun signUp(request: TeacherSignupRequest): Either<List<ValidationError>, TeacherProfile>
}

@Service
class TeacherSignUpUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
    private val teacherSignupRequestValidator: TeacherSignupRequestValidator
) : TeacherSignUpUseCase {

    override fun signUp(request: TeacherSignupRequest): Either<List<ValidationError>, TeacherProfile> {
        val normalizedRequest = normalize(request)
        return teacherSignupRequestValidator.validate(normalizedRequest)
            .map { teacherProfileService.createTeacher(normalizedRequest) }
    }

    private fun normalize(request: TeacherSignupRequest): TeacherSignupRequest {
        return with(request) {
            TeacherSignupRequest(
                email.trim().lowercase(),
                password.trim(),
                firstName.trim(),
                lastName.trim(),
                country.trim(),
                organization.trim(),
                howDidYouHearAboutUs.trim(),
                mobileAppClient
            )
        }
    }

}
