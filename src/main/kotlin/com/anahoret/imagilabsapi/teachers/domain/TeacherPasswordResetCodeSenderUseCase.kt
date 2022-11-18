package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.email.EmailProperties
import com.anahoret.imagilabsapi.email.EmailService
import org.springframework.stereotype.Service

interface TeacherPasswordResetCodeSenderUseCase {

    fun send(email: String, code: String)
}

@Service
class TeacherPasswordResetCodeSenderUseCaseImpl(
    private val emailService: EmailService,
    private val emailProperties: EmailProperties
) : TeacherPasswordResetCodeSenderUseCase {

    override fun send(email: String, code: String) {
        emailService.sendAsync(
            emailProperties.noReplyAddress,
            email,
            "imagi Edu password reset",
            "Verification code: ${code.uppercase()}"
        )
    }

}
