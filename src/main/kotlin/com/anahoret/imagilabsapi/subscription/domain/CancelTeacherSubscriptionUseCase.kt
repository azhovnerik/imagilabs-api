package com.anahoret.imagilabsapi.subscription.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*

interface CancelTeacherSubscriptionUseCase {

    fun cancel(teacherId: UUID): Either<OperationError, Unit>
}

@Service
class CancelTeacherSubscriptionUseCaseImpl(
    private val teacherSubscriptionService: TeacherSubscriptionService,
    private val teacherProfileService: TeacherProfileService
): CancelTeacherSubscriptionUseCase {

    override fun cancel(teacherId: UUID): Either<OperationError, Unit> {
        if (!teacherProfileService.exists(teacherId))
            return NotFoundError("TEACHER_NOT_FOUND").left()

        teacherSubscriptionService.cancelSubscription(teacherId)

        return Unit.right()
    }
}
