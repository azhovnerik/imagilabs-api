package com.anahoret.imagilabsapi.students.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

interface StudentDeleteUseCase {

    fun delete(deleteBy: TeacherProfile, studentId: UUID): Either<OperationError, Unit>
}

@Service
class StudentDeleteUseCaseImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val studentAccessService: StudentAccessService,
    private val studentProfileService: StudentProfileService,
    private val projectService: ProjectService
) : StudentDeleteUseCase {

    @Transactional(rollbackOn = [Throwable::class])
    override fun delete(deleteBy: TeacherProfile, studentId: UUID): Either<OperationError, Unit> {
        val studentProfile = studentProfileService.getStudentById(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()
        if (!studentAccessService.canDelete(deleteBy, studentProfile))
            return AccessDeniedError("ACCESS_TO_STUDENT_DENIED").left()

        projectService.deleteAllByOwner(studentProfile.id)
        studentClassroomLinkService.unlinkFromAll(studentProfile.id)
        studentProfileService.delete(studentProfile.id)
        return Unit.right()
    }
}
