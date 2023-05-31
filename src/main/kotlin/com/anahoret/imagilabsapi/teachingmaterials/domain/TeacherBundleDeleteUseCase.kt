package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import org.springframework.stereotype.Service
import java.util.*

interface TeacherBundleDeleteUseCase {

    fun delete(teacherBundleId: UUID): Either<OperationError, Unit>
}

@Service
class TeacherBundleDeleteUseCaseImpl(
    private val teacherBundleService: TeacherBundleService
): TeacherBundleDeleteUseCase {

    override fun delete(teacherBundleId: UUID): Either<OperationError, Unit> {

        if (!teacherBundleService.exists(teacherBundleId))
            return NotFoundError("TEACHER_BUNDLE_NOT_FOUND").left()

        teacherBundleService.delete(teacherBundleId)

        return Unit.right()
    }
}
