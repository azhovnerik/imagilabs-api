package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomUpdateUseCase {

    fun update(
        updateBy: TeacherProfile,
        classroomId: UUID,
        classroomUpdateRequest: ClassroomUpdateRequest
    ): Either<OperationError, Classroom>

}

@Service
class ClassroomUpdateUseCaseImpl(
    private val classroomAccessService: ClassroomAccessService,
    private val classroomService: ClassroomService
) : ClassroomUpdateUseCase {

    override fun update(
        updateBy: TeacherProfile,
        classroomId: UUID,
        classroomUpdateRequest: ClassroomUpdateRequest
    ): Either<OperationError, Classroom> {
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (!classroomAccessService.canUpdateClassroom(updateBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        return classroomService.update(classroomId, classroomUpdateRequest)?.right()
            ?: NotFoundError("CLASSROOM_NOT_FOUND").left()
    }
}
