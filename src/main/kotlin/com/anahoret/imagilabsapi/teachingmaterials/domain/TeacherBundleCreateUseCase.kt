package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*

interface TeacherBundleCreateUseCase {

    fun create(teacherId: UUID, bundleId: UUID): Either<OperationError, Unit>
}

@Service
class TeacherBundleCreateUseCaseImpl(
    private val teacherBundleService: TeacherBundleService,
    private val teacherProfileService: TeacherProfileService,
    private val lessonBundleService: LessonBundleService
): TeacherBundleCreateUseCase {

    override fun create(teacherId: UUID, bundleId: UUID): Either<OperationError, Unit> {

        if (!teacherProfileService.exists(teacherId))
            return NotFoundError("TEACHER_NOT_FOUND").left()

        if (!lessonBundleService.exists(bundleId))
            return NotFoundError("BUNDLE_NOT_FOUND").left()

        if (lessonBundleService.isDefault(bundleId))
            return ValidationError("DEFAULT_BUNDLE_CANNOT_BE_ADDED").left()

        if (teacherBundleService.hasLinkedBundle(teacherId, bundleId))
            return ValidationError("BUNDLE_HAS_ALREADY_LINKED_TO_TEACHER").left()

        teacherBundleService.create(teacherId, bundleId)

        return Unit.right()
    }
}
