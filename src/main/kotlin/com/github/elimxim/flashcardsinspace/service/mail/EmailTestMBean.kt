package com.github.elimxim.flashcardsinspace.service.mail

import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
import com.github.elimxim.flashcardsinspace.security.SecurityProperties
import com.github.elimxim.flashcardsinspace.security.maskSecret
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedOperationParameter
import org.springframework.jmx.export.annotation.ManagedOperationParameters
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(EmailTestMBean::class.java)

@Component
@Profile("dev")
@ManagedResource(
    objectName = "com.github.elimxim.flashcardsinspace:type=EmailTest",
    description = "MBean for testing email sending in development environment"
)
class EmailTestMBean(
    private val emailService: EmailService,
    private val securityProperties: SecurityProperties,
) {

    @ManagedOperation(description = "Send a test welcome email")
    @ManagedOperationParameters(
        ManagedOperationParameter(name = "email", description = "Recipient email address"),
        ManagedOperationParameter(name = "name", description = "Name to display in the email")
    )
    fun sendTestWelcomeEmail(email: String, name: String): String {
        log.info("JMX: Sending test welcome email to ${maskSecret(email)}")
        return try {
            emailService.sendWelcomeEmail(recipient = Recipient(email, name))
            "Welcome email sent successfully to ${maskSecret(email)}"
        } catch (e: Exception) {
            log.error("JMX: Failed to send test welcome email", e)
            "Failed to send welcome email: ${e.message}"
        }
    }

    @ManagedOperation(description = "Send a test confirmation code email")
    @ManagedOperationParameters(
        ManagedOperationParameter(name = "email", description = "Recipient email address"),
        ManagedOperationParameter(name = "name", description = "Name to display in the email"),
        ManagedOperationParameter(name = "code", description = "Confirmation code to send"),
        ManagedOperationParameter(name = "purpose", description = "Purpose of the confirmation (EMAIL_VERIFICATION)")
    )
    fun sendTestConfirmationCodeEmail(email: String, name: String, code: String, purpose: String): String {
        log.info("JMX: Sending test confirmation code email to ${maskSecret(email)}")
        return try {
            val confirmationPurpose = ConfirmationPurpose.valueOf(purpose)

            val maxAgeSeconds = when (confirmationPurpose) {
                ConfirmationPurpose.EMAIL_VERIFICATION -> securityProperties.verificationCodes.email.maxAge
            }

            emailService.sendConfirmationCodeEmail(
                recipient = Recipient(email, name),
                code,
                confirmationPurpose,
                maxAgeSeconds,
            )
            "Confirmation code email sent successfully to ${maskSecret(email)}"
        } catch (e: Exception) {
            log.error("JMX: Failed to send test confirmation code email", e)
            "Failed to send confirmation code email: ${e.message}"
        }
    }
}

