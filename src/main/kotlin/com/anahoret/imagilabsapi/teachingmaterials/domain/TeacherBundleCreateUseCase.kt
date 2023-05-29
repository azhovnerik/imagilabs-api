package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*

interface TeacherBundleCreateUseCase {

    fun create(teacherId: UUID, request: TeacherBundleCreateRequest): Either<OperationError, Unit>
}

@Service
class TeacherBundleCreateUseCaseImpl(
    private val teacherBundleService: TeacherBundleService,
    private val teacherProfileService: TeacherProfileService,
    private val lessonBundleService: LessonBundleService
): TeacherBundleCreateUseCase {

    override fun create(teacherId: UUID, request: TeacherBundleCreateRequest): Either<OperationError, Unit> {

        if (!teacherProfileService.exists(teacherId))
            return NotFoundError("TEACHER_NOT_FOUND").left()

        if (!lessonBundleService.exists(request.bundleId))
            return NotFoundError("BUNDLE_NOT_FOUND").left()

        teacherBundleService.create(teacherId, request.bundleId)

        return Unit.right()
    }
}
