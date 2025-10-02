package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomGetUseCase {

    fun get(getBy: UserProfile, classroomId: UUID): Either<OperationError, Classroom>
}

@Service
class ClassroomGetUseCaseImpl(
    private val classroomAccessService: ClassroomAccessService,
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService
) : ClassroomGetUseCase {

    override fun get(getBy: UserProfile, classroomId: UUID): Either<OperationError, Classroom> {
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (classroom.blocked)
            return AccessDeniedError("SUBSCRIPTION_REQUIRED").left()

        if (coTeacherService.isLinkedToClassroom(classroom.id, getBy.id)) {
            classroom.teacherRole = TeacherRole.CO_TEACHER
            return classroom.right()
        }

        return if (classroomAccessService.canGetClassroom(getBy, classroom)) {
            classroom.right()
        } else {
            AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()
        }
    }

}
