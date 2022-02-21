package com.anahoret.imagilabsapi.email

import org.slf4j.Logger
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("dev")
class DevEmailService(
    private val logger: Logger
) : EmailService {

    override fun send(from: String, to: List<String>, subject: String, text: String, replyTo: List<String>) {
        logger.info(
            """Message sent
            |FROM: $from
            |TO: $to
            |SUBJECT: $subject
            |TEXT: $text
            |REPLY TO: $replyTo
        """.trimMargin()
        )
    }

    override fun send(from: String, to: String, subject: String, text: String, replyTo: List<String>) {
        send(from, listOf(to), subject, text, replyTo)
    }

    override fun sendAsync(from: String, to: String, subject: String, text: String, replyTo: List<String>) {
        send(from, to, subject, text, replyTo)
    }

}
