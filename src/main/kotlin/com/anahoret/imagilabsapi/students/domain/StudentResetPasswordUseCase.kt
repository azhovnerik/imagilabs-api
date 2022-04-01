package com.anahoret.imagilabsapi.students.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface StudentResetPasswordUseCase {

    fun reset(resetBy: TeacherProfile, studentId: UUID): Either<OperationError, StudentCredentials>
}

@Service
class StudentResetPasswordUseCaseImpl(
    private val studentProfileService: StudentProfileService,
    private val studentAccessService: StudentAccessService
) : StudentResetPasswordUseCase {

    override fun reset(resetBy: TeacherProfile, studentId: UUID): Either<OperationError, StudentCredentials> {
        val studentProfile = studentProfileService.getStudentById(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()

        if (!studentAccessService.canUpdate(resetBy, studentProfile))
            return AccessDeniedError("ACCESS_TO_STUDENT_DENIED").left()

        return studentProfileService.resetPassword(studentId)?.right()
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()
    }
}
