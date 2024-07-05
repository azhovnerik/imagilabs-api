package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationErrors
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.students.domain.StudentUpdateRequest
import com.anahoret.imagilabsapi.students.domain.StudentUpdateRequestValidatorImpl
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherUpdateRequest
import org.springframework.stereotype.Service

interface CompleteOnboardingUseCaseUseCase {

    fun completeOnboarding(userProfile: UserProfile): Either<OperationError, Unit>
}

@Service
class CompleteOnboardingUseCaseUseCaseImpl(
    private val studentProfileService: StudentProfileService,
    private val teacherProfileService: TeacherProfileService,
) : CompleteOnboardingUseCaseUseCase {

    override fun completeOnboarding(userProfile: UserProfile): Either<OperationError, Unit> {
        when (userProfile) {
            is StudentProfile -> {
                studentProfileService.getStudentById(userProfile.id)
                    ?: return NotFoundError("STUDENT_NOT_FOUND").left()
                val studentProfile = studentProfileService.getStudentById(userProfile.id)
                    ?: return NotFoundError("STUDENT_NOT_FOUND").left()
                val studentUpdateRequest = StudentUpdateRequest(studentProfile.name, studentProfile.username, true)
                studentProfileService.update(
                    userProfile.id,
                    studentUpdateRequest
                )?.right() ?: NotFoundError("STUDENT_NOT_FOUND").left()
            }

            is TeacherProfile -> {
                teacherProfileService.getTeacherById(userProfile.id)
                    ?: return NotFoundError("TEACHER_NOT_FOUND").left()
                teacherProfileService.update(userProfile.id, true)?.right() ?: NotFoundError("TEACHER_NOT_FOUND").left()
            }

            else -> {
                return NotFoundError("USER_NOT_FOUND").left()
            }
        }
        return Unit.right()
    }
}
