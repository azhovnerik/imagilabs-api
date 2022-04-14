package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.stereotype.Service

interface TeacherResetPasswordUseCase {

    fun sendVerificationCode(email: String)
    fun resetPassword(teacherResetPasswordRequest: TeacherResetPasswordRequest): Either<OperationError, Unit>
}

@Service
class TeacherResetPasswordUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
    private val teacherPasswordResetCodeSenderUseCase: TeacherPasswordResetCodeSenderUseCase,
    private val teacherPasswordResetService: TeacherPasswordResetService
) : TeacherResetPasswordUseCase {

    override fun sendVerificationCode(email: String) {
        if (teacherProfileService.exists(email)) {
            teacherPasswordResetService.createCode(email)
                ?.let { code -> teacherPasswordResetCodeSenderUseCase.send(email, code) }
        }
    }

    override fun resetPassword(teacherResetPasswordRequest: TeacherResetPasswordRequest): Either<OperationError, Unit> {
        with(teacherResetPasswordRequest) {
            teacherPasswordResetService.getCode(email)
                .takeIf { it == code.lowercase() }
                ?: return ValidationError("WRONG_VERIFICATION_CODE").left()
            teacherProfileService.setPassword(email, newPassword)
            teacherPasswordResetService.resetCode(email)
            return Unit.right()
        }
    }
}
