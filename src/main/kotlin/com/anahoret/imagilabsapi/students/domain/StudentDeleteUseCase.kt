package com.anahoret.imagilabsapi.students.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

interface StudentDeleteUseCase {

    fun delete(deleteBy: TeacherProfile, studentId: UUID): Either<OperationError, Unit>
}

@Service
class StudentDeleteUseCaseImpl(
    private val studentAccessService: StudentAccessService,
    private val studentProfileService: StudentProfileService,
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : StudentDeleteUseCase {

    @Transactional(rollbackOn = [Throwable::class])
    override fun delete(deleteBy: TeacherProfile, studentId: UUID): Either<OperationError, Unit> {
        val studentProfile = studentProfileService.getStudentById(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()

        val classrooms = studentClassroomLinkService.listClassroomsByStudent(studentId)
            .takeIf { it.isNotEmpty() }
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (classrooms.all(Classroom::blocked))
            return AccessDeniedError("SUBSCRIPTION_REQUIRED").left()

        if (!studentAccessService.canDelete(deleteBy, studentProfile))
            return AccessDeniedError("ACCESS_TO_STUDENT_DENIED").left()

        deleteStudentProjects(studentProfile.id)
        studentProfileService.delete(studentProfile.id)
        return Unit.right()
    }

    private fun deleteStudentProjects(studentId: UUID) {
        val studentProjectIds = projectClassroomShareService.listByOwnerId(studentId)
            .map { it.projectId }
        projectClassroomShareService.unshareFromAll(studentProjectIds)
        projectService.deleteAllByOwner(studentId)
    }
}
