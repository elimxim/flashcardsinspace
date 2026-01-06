package com.github.elimxim.flashcardsinspace.service.mail

import brevo.Configuration
import brevo.auth.ApiKeyAuth
import brevoApi.TransactionalEmailsApi
import brevoModel.SendSmtpEmail
import brevoModel.SendSmtpEmailSender
import brevoModel.SendSmtpEmailTo
import com.github.elimxim.flashcardsinspace.security.maskSecret
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val log = LoggerFactory.getLogger(BrevoMailClient::class.java)

class MailClientException(cause: Exception) : RuntimeException(cause)

data class Recipient(val email: String, val name: String)

sealed class Mail(val subject: String, val httpContent: String) {
    class WelcomeMail(subject: String, httpContent: String) : Mail(subject, httpContent)
    class SecurityMail(subject: String, httpContent: String) : Mail(subject, httpContent)
}

@Service
class BrevoMailClient(private val mailProperties: MailProperties) {

    private val welcomeEmail: String
        get() = "welcome@${mailProperties.senderDomain}"

    private val securityEmail: String
        get() = "security@${mailProperties.senderDomain}"

    fun send(recipient: Recipient, mail: Mail) {
        try {
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
                subject = mail.subject
                htmlContent = mail.httpContent
            }

            TransactionalEmailsApi().sendTransacEmail(email)
        } catch (e: Exception) {
            log.error("Failed to send email to ${maskSecret(recipient.email)}", e)
            throw MailClientException(e)
        }
    }

    private fun getSenderEmail(mail: Mail) = when (mail) {
        is Mail.WelcomeMail -> welcomeEmail
        is Mail.SecurityMail -> securityEmail
    }
}
