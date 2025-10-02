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
import org.springframework.stereotype.Service
import java.util.*

interface GetLovableIntegrationForClassroomUseCase {
    fun get(userProfile: UserProfile, classroomId: UUID): Either<OperationError, LovableClassroom>
}

@Service
class GetLovableIntegrationForClassroomUseCaseImpl(
    private val lovableClassroomService: LovableClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val classroomService: ClassroomService
) : GetLovableIntegrationForClassroomUseCase {

    override fun get(userProfile: UserProfile, classroomId: UUID): Either<OperationError, LovableClassroom> {
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (classroom.blocked)
            return AccessDeniedError("SUBSCRIPTION_REQUIRED").left()

        if (!classroomAccessService.canGetClassroom(userProfile, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        return lovableClassroomService.getIntegrationForClassroom(classroomId)?.right()
            ?: NotFoundError("LOVABLE_INTEGRATION_NOT_FOUND").left()
    }
}
