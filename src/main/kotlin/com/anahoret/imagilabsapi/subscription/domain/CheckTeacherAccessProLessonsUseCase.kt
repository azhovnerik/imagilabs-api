package com.anahoret.imagilabsapi.subscription.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.PRO
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.STANDARD
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.time.Clock

interface CheckTeacherAccessProLessonsUseCase {

    fun checkAccess(teacherProfile: TeacherProfile): Either<OperationError, Unit>
}

@Service
class CheckTeacherAccessProLessonsUseCaseImpl(
    private val clock: Clock,
): CheckTeacherAccessProLessonsUseCase {

    override fun checkAccess(teacherProfile: TeacherProfile): Either<OperationError, Unit> {

        val subscription = teacherProfile.subscription

        if (subscription.start == null || subscription.end == null)
            return AccessDeniedError("TEACHER_HAS_NO_SUBSCRIPTION").left()

        val now = clock.instant().toEpochMilli()
        return when (subscription.plan) {
            STANDARD -> AccessDeniedError("TEACHER_MUST_HAVE_SUBSCRIPTION_PRO_PLAN").left()
            PRO -> {
                if (subscription.end < now) AccessDeniedError("TEACHER_SUBSCRIPTION_IS_EXPIRED").left()
                else Unit.right()
            }
        }
    }
}
