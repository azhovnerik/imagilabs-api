package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.lovable.domain.LovableClassroomDeleteUseCase
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomDeleteUseCase {

    fun delete(deleteBy: TeacherProfile, classroomId: UUID): Either<OperationError, Unit>
}

@Service
class ClassroomDeleteUseCaseImpl(
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val studentProfileService: StudentProfileService,
    private val lovableClassroomDeleteUseCase: LovableClassroomDeleteUseCase,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : ClassroomDeleteUseCase {

    @Transactional(rollbackOn = [Throwable::class])
    override fun delete(deleteBy: TeacherProfile, classroomId: UUID): Either<OperationError, Unit> {
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (!classroomAccessService.canDeleteClassroom(deleteBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        lovableClassroomDeleteUseCase.delete(classroomId)

        val studentIds = studentClassroomLinkService.listStudentsByClassroom(classroom.id)
            .map(StudentProfile::id)
        deleteProjects(classroomId, studentIds)
        studentProfileService.delete(studentIds)
        classroomService.delete(classroom.id)

        return Unit.right()
    }

    private fun deleteProjects(classroomId: UUID, studentIds: List<UUID>) {
        projectClassroomShareService.unshareAllFrom(classroomId)
        val projectIds = projectService.listIdsByOwnerIds(studentIds)
        projectService.deleteByIds(projectIds)
    }

}
