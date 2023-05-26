package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import org.springframework.stereotype.Service
import java.util.*

interface CoTeacherLeaveUseCase {

    fun leave(classroomId: UUID, currentTeacherId: UUID): Either<OperationError, Unit>
}

@Service
class CoTeacherLeaveUseCaseImpl(
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService
): CoTeacherLeaveUseCase {

    override fun leave(classroomId: UUID, currentTeacherId: UUID): Either<OperationError, Unit> {

        classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        val coTeacher = coTeacherService.getByClassroomIdAndTeacherId(classroomId, currentTeacherId)
            ?: return AccessDeniedError("CO_TEACHER_MUST_BE_MEMBER_OF_CLASSROOM").left()

        coTeacherService.deleteCoTeacher(coTeacher.id)

        return Unit.right()
    }
}
