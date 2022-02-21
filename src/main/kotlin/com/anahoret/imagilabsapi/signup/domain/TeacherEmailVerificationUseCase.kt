package com.anahoret.imagilabsapi.signup.domain

import org.springframework.stereotype.Service
import java.util.*

interface TeacherEmailVerificationUseCase {

    fun verify(teacherId: UUID, code: String): Boolean
}

@Service
class TeacherEmailVerificationUseCaseImpl(
    private val teacherEmailVerificationCodeService: TeacherEmailVerificationService
) : TeacherEmailVerificationUseCase {

    override fun verify(teacherId: UUID, code: String): Boolean {
        return teacherEmailVerificationCodeService.getTeacherVerificationCode(teacherId)
            ?.let { it == code.trim().lowercase() }
            ?.takeIf { it }
            ?.also { teacherEmailVerificationCodeService.setEmailVerified(teacherId) }
            ?: false
    }

}
