package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service
import java.util.*

interface CoTeacherRemoveUseCase {

    fun remove(classroomId: UUID, invitationId: UUID, currentTeacherId: UUID): Either<OperationError, Unit>
}

@Service
class CoTeacherRemoveUseCaseImpl(
    val classroomService: ClassroomService,
    val coTeacherService: CoTeacherService,
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService
) : CoTeacherRemoveUseCase {

    override fun remove(
        classroomId: UUID,
        invitationId: UUID,
        currentTeacherId: UUID
    ): Either<OperationError, Unit> {

        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        val coTeacher = coTeacherService.getCoTeacher(invitationId)
            ?: return NotFoundError("CO_TEACHER_NOT_FOUND").left()

        if (coTeacher.classroomId != classroomId)
            return AccessDeniedError("CO_TEACHER_MUST_BE_MEMBER_OF_CLASSROOM").left()

        if (currentTeacherId != classroom.teacherId)
            return AccessDeniedError("ONLY_OWNER_CAN_REMOVE_CO_TEACHER").left()

        coTeacherService.deleteCoTeacher(invitationId)

        if (coTeacher.teacherId != null) {
            val projectIds = projectService.getAllIdsByOwnerId(coTeacher.teacherId)
            projectClassroomShareService.unshareProjectsFromClassroom(projectIds, classroomId)
        }

        return Unit.right()
    }
}
