package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.AbstractValidator
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.stereotype.Service

interface ClassroomCreateRequestValidator {

    fun validate(request: ClassroomCreateRequest): Either<List<ValidationError>, Unit>
}

@Service
class ClassroomCreateRequestValidatorImpl
    : ClassroomCreateRequestValidator, AbstractValidator<ClassroomCreateRequest>() {

    override fun validate(request: ClassroomCreateRequest): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            with(request) {
                rejectIfBlank(name, errors, "NAME")
                rejectIfTooLong(name, 50, errors, "NAME")

                if (request.studentCreateRequests.size > 200) {
                    errors.add(ValidationError("TOO_MANY_STUDENTS"))
                }
            }
        }
    }
}
