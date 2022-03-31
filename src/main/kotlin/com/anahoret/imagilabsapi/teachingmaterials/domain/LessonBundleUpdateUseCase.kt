package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationErrors
import org.springframework.stereotype.Service
import java.util.*

interface LessonBundleUpdateUseCase {

    fun update(bundleId: UUID, lessonBundleDataRequest: LessonBundleDataRequest): Either<OperationError, LessonBundle>
}

@Service
class LessonBundleUpdateUseCaseImpl(
    private val lessonBundleDataRequestValidator: LessonBundleDataRequestValidator,
    private val lessonBundleService: LessonBundleService
) : LessonBundleUpdateUseCase {

    override fun update(
        bundleId: UUID,
        lessonBundleDataRequest: LessonBundleDataRequest
    ): Either<OperationError, LessonBundle> {
        return lessonBundleDataRequestValidator.validate(lessonBundleDataRequest)
            .mapLeft(::ValidationErrors)
            .flatMap {
                lessonBundleService.update(bundleId, lessonBundleDataRequest)?.right()
                    ?: NotFoundError("LESSON_BUNDLE_NOT_FOUND").left()
            }
    }
}
