package com.anahoret.imagilabsapi.email

interface EmailService {

    fun send(from: String, to: List<String>, subject: String, text: String, replyTo: List<String> = emptyList())
    fun send(from: String, to: String, subject: String, text: String, replyTo: List<String> = emptyList())
    fun sendAsync(from: String, to: String, subject: String, text: String, replyTo: List<String> = emptyList())
}
