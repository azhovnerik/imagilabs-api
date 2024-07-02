package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.TipTokensResponse
import com.anahoret.imagilabsapi.openai.domain.TipTokensService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service

interface SpendTipTokensUseCase {
    fun spend(userProfile: UserProfile): Either<OperationError, TipTokensResponse>
}

@Service
class SpendTipTokensUseCaseImpl(
    private val studentProfileService: StudentProfileService,
    private val teacherProfileService: TeacherProfileService,
    private val tipTokensService: TipTokensService,
    private val getTipTokensUseCase: GetTipTokensUseCase
) : SpendTipTokensUseCase {

    override fun spend(userProfile: UserProfile): Either<OperationError, TipTokensResponse> {
        when (userProfile) {
            is StudentProfile -> studentProfileService.getStudentById(userProfile.id)
                ?: return NotFoundError("STUDENT_NOT_FOUND").left()

            is TeacherProfile -> teacherProfileService.getTeacherById(userProfile.id)
                ?: return NotFoundError("TEACHER_NOT_FOUND").left()

            else -> return NotFoundError("USER_NOT_FOUND").left()
        }

        tipTokensService.withdrawOneTipToken(userProfile)
        return getTipTokensUseCase.get(userProfile)
    }
}
