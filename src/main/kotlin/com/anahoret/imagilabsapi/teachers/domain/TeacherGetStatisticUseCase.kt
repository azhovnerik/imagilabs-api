package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.teachers.storage.TeacherStatistic
import org.springframework.stereotype.Service
import java.util.*

interface TeacherGetStatisticUseCase {

    fun get(teacherId: UUID): Either<OperationError, TeacherStatistic>
}

@Service
class TeacherGetStatisticUseCaseImpl(
    private val teacherStatisticService: TeacherStatisticService,
    private val teacherProfileService: TeacherProfileService
): TeacherGetStatisticUseCase {

    override fun get(teacherId: UUID): Either<OperationError, TeacherStatistic> {
        if (!teacherProfileService.exists(teacherId))
            return NotFoundError("TEACHER_NOT_FOUND").left()

        val teacherStatistic = teacherStatisticService.getTeacherStatistic(teacherId)
            ?: return NotFoundError("TEACHER_STATISTIC_NOT_FOUND").left()

        return teacherStatistic.right()
    }
}
