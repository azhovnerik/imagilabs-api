package com.anahoret.imagilabsapi.signup.domain

import arrow.core.Either
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.validation.AbstractValidator
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherSignupRequest
import org.springframework.stereotype.Service

interface TeacherSignupRequestValidator {

    fun validate(request: TeacherSignupRequest): Either<List<ValidationError>, Unit>
}

@Service
class TeacherSignupRequestValidatorImpl(
    private val teacherProfileService: TeacherProfileService,
    private val emailValidator: ImagiLabsEmailValidator
) : TeacherSignupRequestValidator, AbstractValidator<TeacherSignupRequest>() {

    override fun validate(request: TeacherSignupRequest): Either<List<ValidationError>, Unit> {
        if (request.isEdLinkSignUp()) return Unit.right()

        return validate { errors ->
            with(request) {
                rejectIfBlank(email, errors, "EMAIL")
                rejectIfTooLong(email, 50, errors, "EMAIL")
                if (!emailValidator.isValid(email)) errors.add(ValidationError.FieldFormatInvalid("EMAIL"))

                rejectIfBlank(password, errors, "PASSWORD")
                rejectIfTooLong(password, 50, errors, "PASSWORD")

                rejectIfBlank(firstName, errors, "FIRST_NAME")
                rejectIfTooLong(firstName, 50, errors, "FIRST_NAME")

                rejectIfBlank(lastName, errors, "LAST_NAME")
                rejectIfTooLong(lastName, 50, errors, "LAST_NAME")

                rejectIfBlank(country, errors, "COUNTRY")
                rejectIfTooLong(country, 50, errors, "COUNTRY")

                rejectIfBlank(organization, errors, "ORGANIZATION")
                rejectIfTooLong(organization, 50, errors, "ORGANIZATION")

                rejectIfBlank(howDidYouHearAboutUs, errors, "HOW_DID_YOU_HEAR_ABOUT_US")
                rejectIfTooLong(howDidYouHearAboutUs, 100, errors, "HOW_DID_YOU_HEAR_ABOUT_US")

                if (teacherProfileService.exists(email)) errors.add(ValidationError("TEACHER_ACCOUNT_ALREADY_EXIST"))
            }
        }
    }

}
