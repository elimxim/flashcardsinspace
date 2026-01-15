package com.github.elimxim.flashcardsinspace.service.mail

import com.github.elimxim.flashcardsinspace.entity.VerificationType
import com.github.elimxim.flashcardsinspace.security.SecurityProperties
import com.github.elimxim.flashcardsinspace.security.getSecurityTokenProperties
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

    @ManagedOperation(description = "Send a test verification code email")
    @ManagedOperationParameters(
        ManagedOperationParameter(name = "email", description = "Recipient email address"),
        ManagedOperationParameter(name = "name", description = "Name to display in the email"),
        ManagedOperationParameter(name = "code", description = "Verification code to send"),
        ManagedOperationParameter(name = "type", description = "Verification intent type")
    )
    fun sendTestVerificationIntentEmail(email: String, name: String, code: String, type: String): String {
        log.info("JMX: Sending test verification code email to ${maskSecret(email)}")
        return try {
            val verificationType = VerificationType.valueOf(type)
            val maxAgeSeconds = securityProperties.getSecurityTokenProperties(verificationType).maxAge

            emailService.sendVerificationIntentEmail(
                recipient = Recipient(email, name),
                code,
                verificationType,
                maxAgeSeconds,
            )
            "Verification code emailed to ${maskSecret(email)}"
        } catch (e: Exception) {
            log.error("JMX: Failed to email test verification code", e)
            "Failed to email verification code: ${e.message}"
        }
    }
}

