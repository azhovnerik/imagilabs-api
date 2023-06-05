package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import org.springframework.stereotype.Service
import java.util.*

interface TeacherBundleDeleteUseCase {

    fun delete(teacherId: UUID, bundleId: UUID): Either<OperationError, Unit>
}

@Service
class TeacherBundleDeleteUseCaseImpl(
    private val teacherBundleService: TeacherBundleService
): TeacherBundleDeleteUseCase {

    override fun delete(teacherId: UUID, bundleId: UUID): Either<OperationError, Unit> {

        val teacherBundleId = teacherBundleService.getByTeacherIdAndBundleId(teacherId, bundleId)
            ?: return NotFoundError("TEACHER_BUNDLE_NOT_FOUND").left()

        teacherBundleService.delete(teacherBundleId)

        return Unit.right()
    }
}
