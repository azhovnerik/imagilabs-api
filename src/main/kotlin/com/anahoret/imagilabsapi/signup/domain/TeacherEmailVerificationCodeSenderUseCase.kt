package com.anahoret.imagilabsapi.signup.domain

import com.anahoret.imagilabsapi.email.EmailProperties
import com.anahoret.imagilabsapi.email.EmailService
import org.springframework.stereotype.Service

interface TeacherEmailVerificationCodeSenderUseCase {

    fun send(email: String, code: String)
}

@Service
class TeacherEmailVerificationCodeSenderUseCaseImpl(
    private val emailService: EmailService,
    private val emailProperties: EmailProperties
) : TeacherEmailVerificationCodeSenderUseCase {

    override fun send(email: String, code: String) {
        emailService.sendAsync(
            emailProperties.noReplyAddress,
            email,
            "Edu Imagilabs email verification",
            "Verification code: ${code.uppercase()}"
        )
    }
}
