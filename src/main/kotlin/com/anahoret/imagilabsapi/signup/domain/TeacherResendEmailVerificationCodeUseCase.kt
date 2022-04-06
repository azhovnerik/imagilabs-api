package com.anahoret.imagilabsapi.signup.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service

interface TeacherResendEmailVerificationCodeUseCase {

    fun resend(teacherProfile: TeacherProfile): Either<OperationError, Unit>
}

@Service
class TeacherResendEmailVerificationCodeUseCaseImpl(
    private val teacherEmailVerificationCodeService: TeacherEmailVerificationService,
    private val teacherEmailVerificationCodeSenderUseCase: TeacherEmailVerificationCodeSenderUseCase,
    private val teacherProfileService: TeacherProfileService
) : TeacherResendEmailVerificationCodeUseCase {

    override fun resend(teacherProfile: TeacherProfile): Either<OperationError, Unit> {
        if (teacherProfile.emailVerified)
            return ValidationError("TEACHER_EMAIL_ALREADY_VERIFIED").left()

        val verificationCode = teacherEmailVerificationCodeService.getTeacherVerificationCode(teacherProfile.id)
            ?: teacherEmailVerificationCodeService.generateNewVerificationCode(teacherProfile.id)
            ?: return NotFoundError("TEACHER_NOT_FOUND").left()

        val teacherEmail = teacherProfileService.getTeacherCredentialsById(teacherProfile.id)?.email
            ?: return NotFoundError("TEACHER_NOT_FOUND").left()

        teacherEmailVerificationCodeSenderUseCase.send(teacherEmail, verificationCode)
        return Unit.right()
    }
}
