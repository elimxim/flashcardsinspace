package com.github.elimxim.flashcardsinspace.service.mail.client

import brevo.Configuration
import brevo.auth.ApiKeyAuth
import brevoApi.TransactionalEmailsApi
import brevoModel.SendSmtpEmail
import brevoModel.SendSmtpEmailReplyTo
import brevoModel.SendSmtpEmailSender
import brevoModel.SendSmtpEmailTo
import com.github.elimxim.flashcardsinspace.service.mail.MailProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnBooleanProperty("app.mail.enabled")
class BrevoMailClient(private val mailProperties: MailProperties): MailClient {

    private val welcomeEmail: String
        get() = "welcome@${mailProperties.senderDomain}"

    private val securityEmail: String
        get() = "security@${mailProperties.senderDomain}"

    override fun send(recipient: Recipient, mail: Mail) {
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
