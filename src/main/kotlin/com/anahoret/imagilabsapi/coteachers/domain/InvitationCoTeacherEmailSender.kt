package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.common.config.DomainProperties
import com.anahoret.imagilabsapi.email.EmailProperties
import com.anahoret.imagilabsapi.email.EmailService
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherEmailSender {

    fun send(email: String, invitationId: UUID)
}

@Service
class InvitationCoTeacherEmailSenderImpl(
    private val emailService: EmailService,
    private val emailProperties: EmailProperties,
    private val domainProperties: DomainProperties
): InvitationCoTeacherEmailSender {

    override fun send(email: String, invitationId: UUID) {
        emailService.sendAsync(
            emailProperties.noReplyAddress,
            email,
            "Imagi Edu invitation",
            "Invitation link: https://${domainProperties.web}/#/invitation/${invitationId}/accept"
        )
    }
}
