package com.anahoret.imagilabsapi.subscription.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface CancelTeacherSubscriptionUseCase {

    fun cancel(teacherId: UUID, userProfile: UserProfile): Either<OperationError, Unit>
}

@Service
class CancelTeacherSubscriptionUseCaseImpl(
    private val teacherSubscriptionService: TeacherSubscriptionService,
    private val teacherProfileService: TeacherProfileService,
    private val exportUseCase: GoogleSheetsTeachersExportUseCase?
): CancelTeacherSubscriptionUseCase {

    override fun cancel(teacherId: UUID, userProfile: UserProfile): Either<OperationError, Unit> {
        if (!teacherProfileService.exists(teacherId))
            return NotFoundError("TEACHER_NOT_FOUND").left()

        return when (userProfile.userType) {
            UserType.ADMIN -> cancelSubscriptionWithExport(teacherId).right()
            else -> {
                if (userProfile.id == teacherId) cancelSubscriptionWithExport(teacherId).right()
                else AccessDeniedError("TEACHER_CAN_CANCEL_ONLY_HIS_SUBSCRIPTION").left()
            }
        }
    }

    private fun cancelSubscriptionWithExport(teacherId: UUID) {
        teacherSubscriptionService.cancelSubscription(teacherId)
            .apply { exportUseCase?.updateAsync(teacherId) }
    }
}
