package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.teacherchecklist.domain.CompleteAccountInformationCheckListStepUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface TeacherProfileUpdateUseCase {
    fun update(teacherId: UUID, request: TeacherProfileUpdateRequest): Either<OperationError, TeacherProfile>
}

@Service
class TeacherProfileUpdateUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
    private val completeAccountInformationCheckListStepUseCase: CompleteAccountInformationCheckListStepUseCase
) : TeacherProfileUpdateUseCase {

    @Transactional
    override fun update(teacherId: UUID, request: TeacherProfileUpdateRequest): Either<OperationError, TeacherProfile> {
        val updatedProfile = teacherProfileService.updateProfile(teacherId, request)
            ?: return NotFoundError("TEACHER_NOT_FOUND").left()

        completeAccountInformationCheckListStepUseCase.checkAndComplete(updatedProfile)

        return updatedProfile.right()
    }
}
