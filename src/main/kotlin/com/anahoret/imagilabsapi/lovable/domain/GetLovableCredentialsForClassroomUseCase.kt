package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import org.springframework.stereotype.Service
import java.util.*


interface GetLovableCredentialsForClassroomUseCase {
    fun getCredentials(userProfile: UserProfile, classroomId: UUID): Either<OperationError, List<LovableAccount>>
}

@Service
class GetLovableCredentialsForClassroomUseCaseImpl(
    private val classroomAccessService: ClassroomAccessService,
    private val classroomService: ClassroomService,
    private val lovableAccountService: LovableAccountService,
    private val studentProfileService: StudentProfileService
) : GetLovableCredentialsForClassroomUseCase {
    override fun getCredentials(
        userProfile: UserProfile,
        classroomId: UUID
    ): Either<OperationError, List<LovableAccount>> {
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (classroom.blocked)
            return AccessDeniedError("SUBSCRIPTION_REQUIRED").left()

        if (!classroomAccessService.canListStudentCredentials(userProfile, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        val studentIds = studentProfileService.listByClassroom(classroomId).map(StudentProfile::id)

        return lovableAccountService.getByConnectedUsers(studentIds).right()
    }


}
