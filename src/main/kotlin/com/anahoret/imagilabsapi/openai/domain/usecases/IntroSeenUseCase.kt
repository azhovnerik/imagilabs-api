package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service

interface IntroSeenUseCase {

    fun setIntroSeen(teacherProfile: TeacherProfile): Either<OperationError, Unit>
}

@Service
class IntroSeenUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
) : IntroSeenUseCase {

    override fun setIntroSeen(teacherProfile: TeacherProfile): Either<OperationError, Unit> {
        teacherProfileService.setIntroSeen(teacherProfile.id)?.right()
            ?: NotFoundError("TEACHER_NOT_FOUND").left()
        return Unit.right()
    }
}
