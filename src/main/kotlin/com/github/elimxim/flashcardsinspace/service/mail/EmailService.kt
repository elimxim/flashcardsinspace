package com.github.elimxim.flashcardsinspace.service.mail

import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
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
                mail = Mail.WelcomeMail(subject = "Welcome to Flashcards from Space!", httpContent = htmlContent)
            )
            log.info("Welcome email sent to ${maskSecret(recipient.email)}")
        } catch (e: Exception) {
            log.error("Failed to send welcome email to ${maskSecret(recipient.email)}", e)
        }
    }

    fun sendConfirmationCodeEmail(recipient: Recipient, code: String, purpose: ConfirmationPurpose) {
        log.info("Sending confirmation code to ${maskSecret(recipient.email)}, purpose: $purpose")
        try {
            val context = Context().apply {
                setVariable("code", code)
                setVariable("purpose", purpose.name)
                setVariable("purposeDescription", getPurposeDescription(purpose))
            }
            val htmlContent = templateEngine.process("confirmation-code-email", context)

            mailClient.send(
                recipient = recipient,
                mail = Mail.SecurityMail(getSubjectForPurpose(purpose), httpContent = htmlContent)
            )
            log.info("Confirmation code email sent to ${maskSecret(recipient.email)}")
        } catch (e: Exception) {
            log.error("Failed to send confirmation code email to ${maskSecret(recipient.email)}", e)
            throw e
        }
    }

    private fun getSubjectForPurpose(purpose: ConfirmationPurpose): String {
        return when (purpose) {
            ConfirmationPurpose.EMAIL_VERIFICATION -> "Verify Your Email - Flashcards in Space"
        }
    }

    private fun getPurposeDescription(purpose: ConfirmationPurpose): String {
        return when (purpose) {
            ConfirmationPurpose.EMAIL_VERIFICATION -> "verify your email address"
        }
    }
}
