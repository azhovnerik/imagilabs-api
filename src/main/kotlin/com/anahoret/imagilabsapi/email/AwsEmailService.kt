package com.anahoret.imagilabsapi.email

import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
@Profile("prod", "stage")
class AwsEmailService(
    private val javaMailSender: JavaMailSender
) : EmailService {

    @Async
    override fun sendAsync(from: String, to: String, subject: String, text: String, replyTo: List<String>) {
        val mimeMessage = javaMailSender.createMimeMessage()
        mimeMessage.setRecipients(Message.RecipientType.TO, arrayOf(InternetAddress(to)))
        mimeMessage.setFrom(from)
        mimeMessage.setSubject(subject, Charsets.UTF_8.name())
        mimeMessage.setText(text, Charsets.UTF_8.name())
        if (replyTo.isNotEmpty()) {
            mimeMessage.replyTo = replyTo.map(::InternetAddress).toTypedArray()
        }
        javaMailSender.send(mimeMessage)
    }

}
