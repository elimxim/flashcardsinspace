package com.github.elimxim.flashcardsinspace.service.mail

import com.github.elimxim.flashcardsinspace.entity.VerificationType
import com.github.elimxim.flashcardsinspace.security.maskSecret
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

private val log = LoggerFactory.getLogger(EmailService::class.java)

@Service
class EmailService(
    private val mailClient: BrevoMailClient,
    private val templateEngine: TemplateEngine
) {

    fun sendWelcomeEmail(recipient: Recipient) {
        log.info("Sending welcome email to ${maskSecret(recipient.email)}")
        try {
            val context = Context().apply {
                setVariable("name", recipient.name)
            }

            val htmlContent = templateEngine.process("welcome-email", context)

            mailClient.send(
                recipient = recipient,
                mail = Mail.WelcomeMail(subject = "Welcome to Flashcards in Space!", htmlContent = htmlContent)
            )
            log.info("Welcome email sent to ${maskSecret(recipient.email)}")
        } catch (e: Exception) {
            log.error("Failed to send welcome email to ${maskSecret(recipient.email)}", e)
        }
    }

    fun sendVerificationIntentEmail(
        recipient: Recipient,
        code: String,
        type: VerificationType,
        maxAgeSeconds: Int
    ) {
        log.info("Sending verification code to ${maskSecret(recipient.email)}, type: $type")
        val context = Context().apply {
            setVariable("securityCodeDigits", code.toList().map { it.toString() })
            setVariable("subtitle", getSubtitleForType(type))
            setVariable("expirationTime", getExpirationTime(maxAgeSeconds))
        }
        val htmlContent = templateEngine.process("verification-code-email", context)

        mailClient.send(
            recipient = recipient,
            mail = Mail.SecurityMail(getSubjectForType(type), htmlContent = htmlContent)
        )
        log.info("Verification code emailed to ${maskSecret(recipient.email)}")
    }

    private fun getSubjectForType(type: VerificationType): String {
        return when (type) {
            VerificationType.REGISTRATION_REQUEST -> "Complete Registration - Flashcards in Space"
            VerificationType.PASSWORD_RESET_ACCESS -> "Reset Your Password - Flashcards in Space"
            VerificationType.PASSWORD_RESET_REQUEST -> "Reset Your Password - Flashcards in Space"
        }
    }

    private fun getSubtitleForType(type: VerificationType): String {
        return when (type) {
            VerificationType.REGISTRATION_REQUEST -> "Verify Your Email"
            VerificationType.PASSWORD_RESET_ACCESS -> "Reset Your Password"
            VerificationType.PASSWORD_RESET_REQUEST -> "Reset Your Password"
        }
    }

    private fun getExpirationTime(maxAgeSeconds: Int): String {
        return when (maxAgeSeconds) {
            in 0..60 -> "1 minute"
            in 61..3599 -> "${maxAgeSeconds / 60} minutes"
            3600 -> "1 hour"
            in 3601..86399 -> "${maxAgeSeconds / 3600} hours"
            86400 -> "1 day"
            in 86401..604800 -> "${maxAgeSeconds / 86400} days"
            else -> "a week"
        }
    }
}
