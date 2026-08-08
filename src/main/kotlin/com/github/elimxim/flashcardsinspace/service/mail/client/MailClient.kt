package com.github.elimxim.flashcardsinspace.service.mail.client

data class Recipient(val email: String, val name: String? = null)

sealed class Mail(val subject: String, val htmlContent: String) {
    class WelcomeMail(subject: String, htmlContent: String) : Mail(subject, htmlContent)
    class SecurityMail(subject: String, htmlContent: String) : Mail(subject, htmlContent)
}

interface MailClient {
    fun send(recipient: Recipient, mail: Mail)
}
