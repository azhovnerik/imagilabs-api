package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.AbstractValidator
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.stereotype.Service

interface TeacherLessonsUpdateRequestValidator {

    fun validate(teacherLessonsUpdateRequest: TeacherLessonsUpdateRequest): Either<List<ValidationError>, Unit>
}

@Service
class TeacherLessonsUpdateRequestValidatorImpl :
    TeacherLessonsUpdateRequestValidator, AbstractValidator<TeacherLessonsUpdateRequest>() {

    override fun validate(teacherLessonsUpdateRequest: TeacherLessonsUpdateRequest): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            val indices = teacherLessonsUpdateRequest.lessons.map { it.index }.sorted()
            val expectedIndices = (indices.indices).toList()
            if (indices != expectedIndices) {
                errors.add(ValidationError("INDICES_SHOULD_BE_ORDERED_FROM_ZERO"))
            }
        }
    }

}
