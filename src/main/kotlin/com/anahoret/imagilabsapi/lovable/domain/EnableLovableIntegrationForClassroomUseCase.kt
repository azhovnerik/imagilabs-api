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

interface EnableLovableIntegrationForClassroomUseCase {
    fun enable(userProfile: UserProfile, classroomId: UUID): Either<OperationError, LovableClassroom>
}

@Service
class EnableLovableIntegrationForClassroomUseCaseImpl(
    private val lovableClassroomService: LovableClassroomService,
    private val studentProfileService: StudentProfileService,
    private val lovableAccountService: LovableAccountService,
    private val connectLovableAccountToUserUseCase: ConnectLovableAccountToUserUseCase,
    private val classroomAccessService: ClassroomAccessService,
    private val classroomService: ClassroomService,
    private val getLovableIntegrationForClassroomUseCase: GetLovableIntegrationForClassroomUseCase
) : EnableLovableIntegrationForClassroomUseCase {

    override fun enable(userProfile: UserProfile, classroomId: UUID): Either<OperationError, LovableClassroom> {
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (!classroomAccessService.canUpdateClassroom(userProfile, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        lovableClassroomService.enableIntegrationForClassroom(classroomId)
        studentProfileService.listByClassroom(classroomId)
            .filter { lovableAccountService.getActive(it) == null }
            .forEach { connectLovableAccountToUserUseCase.connect(it) }

        return getLovableIntegrationForClassroomUseCase.get(userProfile, classroomId)
    }
}
