package com.anahoret.imagilabsapi.signup.domain

import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*

interface TeacherEmailVerificationUseCase {

    fun verify(teacherId: UUID, code: String): TeacherProfile?
}

@Service
class TeacherEmailVerificationUseCaseImpl(
    private val teacherEmailVerificationCodeService: TeacherEmailVerificationService,
    private val teacherProfileService: TeacherProfileService
) : TeacherEmailVerificationUseCase {

    override fun verify(teacherId: UUID, code: String): TeacherProfile? {
        return teacherEmailVerificationCodeService.getTeacherVerificationCode(teacherId)
            ?.let { it == code.trim().lowercase() }
            ?.takeIf { it }
            ?.also { teacherEmailVerificationCodeService.setEmailVerified(teacherId) }
            ?.let { teacherProfileService.getTeacherById(teacherId) }
    }

}
