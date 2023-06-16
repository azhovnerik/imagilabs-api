package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.common.config.DomainProperties
import com.anahoret.imagilabsapi.email.EmailProperties
import com.anahoret.imagilabsapi.email.EmailService
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherEmailSender {

    fun send(preferences: InvitationEmailPreferences)
}

@Service
class InvitationCoTeacherEmailSenderImpl(
    private val emailService: EmailService,
    private val emailProperties: EmailProperties,
    private val domainProperties: DomainProperties
): InvitationCoTeacherEmailSender {

    override fun send(preferences: InvitationEmailPreferences) {
        with(preferences) {
            emailService.sendAsync(
                emailProperties.noReplyAddress,
                sendTo,
                "imagi Edu Invitation from $fromName to join their classroom",
                """
                Hello,

                You have been invited by $from to join them as a co-teacher on the imagi Edu platform. 

                Accept the invitation here: https://${domainProperties.web}/#/invitation/$invitationId/accept

                Please note that in order to access this classroom you need an imagi Edu Pro account linked to this email address.

                If you need any help, reach out to support@imagilabs.com. 

                Welcome!
                the imagi team
            """.trimIndent()
            )
        }
    }
}
