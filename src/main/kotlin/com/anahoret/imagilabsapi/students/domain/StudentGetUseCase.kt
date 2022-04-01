package com.anahoret.imagilabsapi.students.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import org.springframework.stereotype.Service
import java.util.*

interface StudentGetUseCase {

    fun get(getBy: UserProfile, studentId: UUID): Either<OperationError, StudentDetails>
}

@Service
class StudentGetUseCaseImpl(
    private val studentProfileService: StudentProfileService,
    private val studentAccessService: StudentAccessService
) : StudentGetUseCase {

    override fun get(getBy: UserProfile, studentId: UUID): Either<OperationError, StudentDetails> {
        val studentDetails = studentProfileService.getStudentDetailsById(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()

        if (!studentAccessService.canGet(getBy, studentDetails))
            return AccessDeniedError("ACCESS_TO_STUDENT_DENIED").left()

        return studentDetails.right()
    }
}
