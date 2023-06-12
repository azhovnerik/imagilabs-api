package com.anahoret.imagilabsapi.subscription.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import org.springframework.stereotype.Service
import java.util.*

interface SetSubscriptionPeriodUseCase {
    fun set(teacherId: UUID, request: SetSubscriptionPeriodRequest): Either<OperationError, Unit>
}

@Service
class SetSubscriptionPeriodUseCaseImpl(
    private val teacherSubscriptionService: TeacherSubscriptionService,
    private val teacherProfileService: TeacherProfileService,
    private val googleSheetsTeachersExportUseCase: GoogleSheetsTeachersExportUseCase?
) : SetSubscriptionPeriodUseCase {
    override fun set(teacherId: UUID, request: SetSubscriptionPeriodRequest): Either<OperationError, Unit> {
        if (request.endDate < request.startDate)
            return SubscriptionPeriodInvalid("END_DATE_SHOULD_BE_AFTER_START_DATE").left()

        if (!teacherProfileService.exists(teacherId))
            return NotFoundError("TEACHER_NOT_FOUND").left()

        teacherSubscriptionService.setPeriod(teacherId, request.startDate, request.endDate)
        googleSheetsTeachersExportUseCase?.updateAsync(teacherId)

        return Unit.right()
    }

}

class SubscriptionPeriodInvalid(message: String): ValidationError(message)


