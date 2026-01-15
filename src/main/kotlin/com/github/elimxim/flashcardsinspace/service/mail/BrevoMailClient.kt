package com.github.elimxim.flashcardsinspace.service.mail

import brevo.Configuration
import brevo.auth.ApiKeyAuth
import brevoApi.TransactionalEmailsApi
import brevoModel.SendSmtpEmail
import brevoModel.SendSmtpEmailReplyTo
import brevoModel.SendSmtpEmailSender
import brevoModel.SendSmtpEmailTo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val log = LoggerFactory.getLogger(BrevoMailClient::class.java)

class MailClientException(cause: Exception) : RuntimeException(cause)

data class Recipient(val email: String, val name: String? = null)

sealed class Mail(val subject: String, val htmlContent: String) {
    class WelcomeMail(subject: String, htmlContent: String) : Mail(subject, htmlContent)
    class SecurityMail(subject: String, htmlContent: String) : Mail(subject, htmlContent)
}

@Service
class BrevoMailClient(private val mailProperties: MailProperties) {

    private val welcomeEmail: String
        get() = "welcome@${mailProperties.senderDomain}"

    private val securityEmail: String
        get() = "security@${mailProperties.senderDomain}"

    fun send(recipient: Recipient, mail: Mail) {
        val apiClient = Configuration.getDefaultApiClient()
        val auth = apiClient.getAuthentication("api-key") as ApiKeyAuth
        auth.apiKey = mailProperties.apiKey


        val email = SendSmtpEmail().apply {
            sender = SendSmtpEmailSender()
                .email(getSenderEmail(mail))
                .name(mailProperties.senderName)
            to = listOf(
                SendSmtpEmailTo()
                    .email(recipient.email)
                    .name(recipient.name)
            )
            replyTo = SendSmtpEmailReplyTo()
                .email(mailProperties.replyToEmail)
            subject = mail.subject
            htmlContent = mail.htmlContent
        }

        TransactionalEmailsApi().sendTransacEmail(email)
    }

    private fun getSenderEmail(mail: Mail) = when (mail) {
        is Mail.WelcomeMail -> welcomeEmail
        is Mail.SecurityMail -> securityEmail
    }
}
