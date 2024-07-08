package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.students.domain.StudentUpdateRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service

interface CompleteOnboardingUseCase {

    fun completeOnboarding(userProfile: UserProfile): Either<OperationError, Unit>
}

@Service
class CompleteOnboardingUseCaseUseCaseImpl(
    private val studentProfileService: StudentProfileService,
    private val teacherProfileService: TeacherProfileService,
) : CompleteOnboardingUseCase {

    override fun completeOnboarding(userProfile: UserProfile): Either<OperationError, Unit> {
        when (userProfile) {
            is StudentProfile ->
                studentProfileService.completeChatOnboarding(
                    userProfile.id
                )?.right() ?: NotFoundError("STUDENT_NOT_FOUND").left()

            is TeacherProfile ->
                teacherProfileService.completeChatOnboarding(userProfile.id)?.right()
                    ?: NotFoundError("TEACHER_NOT_FOUND").left()

            else -> {
                return NotFoundError("USER_NOT_FOUND").left()
            }
        }
        return Unit.right()
    }
}
