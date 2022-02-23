package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.stereotype.Service

interface ClassroomCreateRequestValidator {

    fun validate(classroomCreateRequest: ClassroomCreateRequest): Either<List<ValidationError>, Unit>
}

@Service
class ClassroomCreateRequestValidatorImpl : ClassroomCreateRequestValidator {

    override fun validate(classroomCreateRequest: ClassroomCreateRequest): Either<List<ValidationError>, Unit> {
        TODO("not implemented")
    }
}
