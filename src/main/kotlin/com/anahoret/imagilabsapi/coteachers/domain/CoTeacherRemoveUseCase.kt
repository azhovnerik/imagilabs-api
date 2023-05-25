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

interface CoTeacherRemoveUseCase {

    fun remove(classroomId: UUID, coTeacherId: UUID, currentTeacherId: UUID): Either<OperationError, Unit>
}

@Service
class CoTeacherRemoveUseCaseImpl(
    val classroomService: ClassroomService,
    val coTeacherService: CoTeacherService
) : CoTeacherRemoveUseCase {

    override fun remove(
        classroomId: UUID,
        coTeacherId: UUID,
        currentTeacherId: UUID
    ): Either<OperationError, Unit> {

        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (!coTeacherService.existsById(coTeacherId))
            return NotFoundError("CO_TEACHER_NOT_FOUND").left()

        if (currentTeacherId != classroom.teacherId)
            return AccessDeniedError("ONLY_OWNER_CAN_REMOVE_CO_TEACHER").left()

        coTeacherService.deleteCoTeacher(coTeacherId)

        return Unit.right()
    }
}
