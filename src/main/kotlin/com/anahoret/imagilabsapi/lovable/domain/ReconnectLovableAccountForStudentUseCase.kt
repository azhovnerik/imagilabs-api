package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import org.springframework.stereotype.Service
import java.util.*

interface ReconnectLovableAccountForStudentUseCase {
    fun reconnect(userProfile: UserProfile, studentId: UUID): Either<OperationError, LovableAccount>
}

@Service
class ReconnectLovableAccountForStudentUseCaseImpl(
    private val classroomAccessService: ClassroomAccessService,
    private val classroomService: ClassroomService,
    private val connectLovableAccountToUserUseCase: ConnectLovableAccountToUserUseCase,
    private val studentProfileService: StudentProfileService
) : ReconnectLovableAccountForStudentUseCase {

    override fun reconnect(userProfile: UserProfile, studentId: UUID): Either<OperationError, LovableAccount> {
        val student = studentProfileService.getStudentById(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()

        val classroom = classroomService.getById(student.classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (classroom.blocked)
            return AccessDeniedError("SUBSCRIPTION_REQUIRED").left()

        if (!classroomAccessService.canUpdateClassroom(userProfile, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        return connectLovableAccountToUserUseCase.connect(student)
    }
}
