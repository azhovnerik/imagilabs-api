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

interface SetPausedLovableIntegrationForClassroomUseCase {
    fun setPaused(userProfile: UserProfile, classroomId: UUID, paused: Boolean): Either<OperationError, Unit>
}

@Service
class SetPausedLovableIntegrationForClassroomUseCaseImpl(
    private val lovableClassroomService: LovableClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val classroomService: ClassroomService
) : SetPausedLovableIntegrationForClassroomUseCase {
    override fun setPaused(userProfile: UserProfile, classroomId: UUID, paused: Boolean): Either<OperationError, Unit> {
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (classroom.blocked)
            return AccessDeniedError("SUBSCRIPTION_REQUIRED").left()

        if (!classroomAccessService.canUpdateClassroom(userProfile, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        lovableClassroomService.setPausedIntegrationForClassroom(classroomId, paused)

        return Unit.right()
    }
}
