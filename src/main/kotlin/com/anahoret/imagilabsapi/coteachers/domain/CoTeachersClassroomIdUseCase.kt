package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import org.springframework.stereotype.Service
import java.util.*

interface CoTeachersClassroomIdUseCase {

    fun getAll(classroomId: UUID): Either<OperationError, List<CoTeacher>>
}

@Service
class CoTeachersClassroomIdUseCaseImpl(
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService
): CoTeachersClassroomIdUseCase {

    override fun getAll(classroomId: UUID): Either<OperationError, List<CoTeacher>> {
        classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        return coTeacherService.getAllCoTeachersByClassroomId(classroomId).right()
    }
}
