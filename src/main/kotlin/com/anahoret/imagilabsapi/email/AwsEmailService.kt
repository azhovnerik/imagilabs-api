package com.anahoret.imagilabsapi.email

import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import javax.mail.Message
import javax.mail.internet.InternetAddress

@Service
@Profile("prod")
class AwsEmailService(
    private val javaMailSender: JavaMailSender
) : EmailService {

    override fun send(from: String, to: String, subject: String, text: String, replyTo: List<String>) {
        send(from, listOf(to), subject, text, replyTo)
    }

    override fun send(from: String, to: List<String>, subject: String, text: String, replyTo: List<String>) {
        val mimeMessage = javaMailSender.createMimeMessage()
        mimeMessage.setRecipients(Message.RecipientType.TO, to.map { InternetAddress(it) }.toTypedArray())
        mimeMessage.setFrom(from)
        mimeMessage.setSubject(subject, Charsets.UTF_8.name())
        mimeMessage.setText(text, Charsets.UTF_8.name())
        if (replyTo.isNotEmpty()) {
            mimeMessage.replyTo = replyTo.map(::InternetAddress).toTypedArray()
        }
        javaMailSender.send(mimeMessage)
    }

    override fun sendAsync(from: String, to: String, subject: String, text: String, replyTo: List<String>) {
        CompletableFuture.runAsync { send(from, to, subject, text, replyTo) }
    }

}
