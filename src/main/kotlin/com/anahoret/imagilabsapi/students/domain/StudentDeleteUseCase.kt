package com.anahoret.imagilabsapi.students.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.SystemProfile
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

interface StudentDeleteUseCase {

    fun delete(deleteBy: UserProfile, studentId: UUID): Either<OperationError, Unit>
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
    override fun delete(deleteBy: UserProfile, studentId: UUID): Either<OperationError, Unit> {
        val studentProfile = studentProfileService.getStudentById(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()

        val classrooms = studentClassroomLinkService.listClassroomsByStudent(studentId)
            .takeIf { it.isNotEmpty() }
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        when (deleteBy) {
            is TeacherProfile -> deleteByTeacher(deleteBy, classrooms, studentProfile)
            is SystemProfile -> doDelete(studentProfile, classrooms)
            else -> return AccessDeniedError("ACCESS_DENIED").left()
        }

        return Unit.right()
    }

    private fun deleteByTeacher(
        deleteBy: TeacherProfile,
        classrooms: List<Classroom>,
        studentProfile: StudentProfile
    ): Either<OperationError, Unit> {
        if (classrooms.all(Classroom::blocked))
            return AccessDeniedError("SUBSCRIPTION_REQUIRED").left()

        if (!studentAccessService.canDelete(deleteBy, studentProfile))
            return AccessDeniedError("ACCESS_TO_STUDENT_DENIED").left()

        doDelete(studentProfile, classrooms)
        return Unit.right()
    }

    private fun doDelete(studentProfile: StudentProfile, classrooms: List<Classroom>) {
        deleteStudentProjects(studentProfile.id)
        classrooms.forEach { classroom ->
            studentClassroomLinkService.removeStudentFromClassroom(studentProfile.id, classroom.id)
        }
        studentProfileService.delete(studentProfile.id)
    }

    private fun deleteStudentProjects(studentId: UUID) {
        val studentProjectIds = projectClassroomShareService.listByOwnerId(studentId)
            .map { it.projectId }
        projectClassroomShareService.unshareFromAll(studentProjectIds)
        projectService.deleteAllByOwner(studentId)
    }
}
