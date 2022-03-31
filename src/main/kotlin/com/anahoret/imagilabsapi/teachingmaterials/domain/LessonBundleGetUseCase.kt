package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import org.springframework.stereotype.Service
import java.util.*

interface LessonBundleGetUseCase {

    fun get(bundleId: UUID): Either<OperationError, LessonBundle>
}

@Service
class LessonBundleGetUseCaseImpl(
    private val lessonBundleService: LessonBundleService
) : LessonBundleGetUseCase {

    override fun get(bundleId: UUID): Either<OperationError, LessonBundle> {
        return lessonBundleService.get(bundleId)?.right()
            ?: NotFoundError("LESSON_BUNDLE_NOT_FOUND").left()
    }
}
