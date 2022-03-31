package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationErrors
import org.springframework.stereotype.Service

interface LessonBundleCreateUseCase {

    fun create(lessonBundleDataRequest: LessonBundleDataRequest): Either<OperationError, LessonBundle>
}

@Service
class LessonBundleCreateUseCaseImpl(
    private val lessonBundleDataRequestValidator: LessonBundleDataRequestValidator,
    private val lessonBundleService: LessonBundleService
) : LessonBundleCreateUseCase {

    override fun create(lessonBundleDataRequest: LessonBundleDataRequest): Either<OperationError, LessonBundle> {
        return lessonBundleDataRequestValidator.validate(lessonBundleDataRequest)
            .mapLeft(::ValidationErrors)
            .map { lessonBundleService.create(lessonBundleDataRequest) }
    }
}
