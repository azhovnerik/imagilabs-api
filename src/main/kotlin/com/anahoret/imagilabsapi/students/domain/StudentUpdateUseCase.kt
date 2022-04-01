package com.anahoret.imagilabsapi.students.domain

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationErrors
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface StudentUpdateUseCase {

    fun update(
        updateBy: TeacherProfile,
        studentId: UUID,
        studentUpdateRequest: StudentUpdateRequest
    ): Either<OperationError, StudentProfile>
}

@Service
class StudentUpdateUseCaseImpl(
    private val studentAccessService: StudentAccessService,
    private val studentProfileService: StudentProfileService,
    private val studentUpdateRequestValidator: StudentUpdateRequestValidator
) : StudentUpdateUseCase {

    override fun update(
        updateBy: TeacherProfile,
        studentId: UUID,
        studentUpdateRequest: StudentUpdateRequest
    ): Either<OperationError, StudentProfile> {
        val studentProfile = studentProfileService.getStudentById(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()
        val currentCredentials = studentProfileService.getStudentCredentials(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()
        if (!studentAccessService.canUpdate(updateBy, studentProfile))
            return AccessDeniedError("ACCESS_TO_STUDENT_DENIED").left()
        return studentUpdateRequestValidator.validate(studentProfile.id, currentCredentials, studentUpdateRequest)
            .mapLeft(::ValidationErrors)
            .flatMap {
                studentProfileService.update(studentId, studentUpdateRequest)?.right()
                    ?: NotFoundError("STUDENT_NOT_FOUND").left()
            }
    }
}
