package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.AbstractValidator
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.stereotype.Service

interface LessonBundleDataRequestValidator {

    fun validate(lessonBundleDataRequest: LessonBundleDataRequest): Either<List<ValidationError>, Unit>
}

@Service
class LessonBundleDataRequestValidatorImpl
    : LessonBundleDataRequestValidator, AbstractValidator<LessonBundleDataRequest>() {

    override fun validate(lessonBundleDataRequest: LessonBundleDataRequest): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            rejectIfBlank(lessonBundleDataRequest.name, errors, "BUNDLE_NAME")
            val indices = lessonBundleDataRequest.lessons.map { it.index }.sorted()
            val expectedIndices = (indices.indices).toList()
            if (indices != expectedIndices) {
                errors.add(ValidationError("INDICES_SHOULD_BE_ORDERED_FROM_ZERO"))
            }
        }
    }

}
