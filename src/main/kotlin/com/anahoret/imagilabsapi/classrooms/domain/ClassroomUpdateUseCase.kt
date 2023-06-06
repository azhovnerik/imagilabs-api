package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationErrors
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import jakarta.transaction.Transactional
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
    private val classroomService: ClassroomService,
    private val classroomValidator: ClassroomValidator,
    private val studentProfileService: StudentProfileService,
    private val coTeacherService: CoTeacherService
) : ClassroomUpdateUseCase {

    @Transactional(rollbackOn = [Throwable::class])
    override fun update(
        updateBy: TeacherProfile,
        classroomId: UUID,
        classroomUpdateRequest: ClassroomUpdateRequest
    ): Either<OperationError, Classroom> {
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (!classroomAccessService.canUpdateClassroom(updateBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        return classroomValidator.validate(updateBy, classroomId, classroomUpdateRequest)
            .mapLeft(::ValidationErrors)
            .flatMap { doUpdateClassroom(classroomId, classroomUpdateRequest, updateBy) }
    }

    private fun doUpdateClassroom(
        classroomId: UUID,
        classroomUpdateRequest: ClassroomUpdateRequest,
        updateBy: TeacherProfile
    ): Either<OperationError, Classroom> {
        val classroom = classroomService.update(classroomId, classroomUpdateRequest)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        studentProfileService.createStudents(classroom.id, classroomUpdateRequest.studentCreateRequests)

        if (coTeacherService.isCoClassroom(classroom.id, updateBy.id))
            classroom.teacherRole = TeacherRole.CO_TEACHER

        return classroom.right()
    }

}
