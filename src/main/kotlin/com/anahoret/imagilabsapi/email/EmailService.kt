package com.anahoret.imagilabsapi.email

interface EmailService {

    fun sendAsync(from: String, to: String, subject: String, text: String, replyTo: List<String> = emptyList())
}
