package com.github.elimxim.flashcardsinspace.service.mail

import com.github.elimxim.flashcardsinspace.AppConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.validation.BindValidationException
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.mock.env.MockEnvironment
import org.springframework.validation.BeanPropertyBindingResult

class MailPropertiesValidatorTest {
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(AppConfig::class.java)
        .withPropertyValues(
            "app.mail.api-key=dummy-api-key",
            "app.mail.sender-name=test-name",
            "app.mail.sender-domain=test-domain-com",
            "app.mail.reply-to-email=no-reply-dot-com",
        )

    @Test
    fun `should pass if mail is enabled without the dev profile`() {
        val errors = validate(mailProperties(enabled = true), MockEnvironment())

        assertThat(errors.hasErrors()).isFalse()
    }

    @Test
    fun `should pass if mail is disabled with the dev profile`() {
        val environment = MockEnvironment().apply { setActiveProfiles("docker", "dev") }

        val errors = validate(mailProperties(enabled = false), environment)

        assertThat(errors.hasErrors()).isFalse()
    }

    @Test
    fun `should reject enabled if mail is disabled without the dev profile`() {
        val environment = MockEnvironment().apply { setActiveProfiles("docker") }

        val errors = validate(mailProperties(enabled = false), environment)

        assertThat(errors.fieldErrors.map { it.field }).containsExactly("enabled")
    }

    @Test
    fun `should fail startup naming the property if mail is disabled without the dev profile`() {
        contextRunner
            .withPropertyValues("app.mail.enabled=false")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).rootCause()
                    .isInstanceOf(BindValidationException::class.java)
                    .hasMessageContaining("app.mail.enabled")
            }
    }

    @Test
    fun `should start if mail is disabled with the dev profile`() {
        contextRunner
            .withPropertyValues("app.mail.enabled=false", "spring.profiles.active=dev")
            .run { context ->
                assertThat(context).hasNotFailed()
            }
    }

    private fun validate(properties: MailProperties, environment: MockEnvironment) =
        BeanPropertyBindingResult(properties, "app.mail").also {
            MailPropertiesValidator(environment).validate(properties, it)
        }

    private fun mailProperties(enabled: Boolean) = MailProperties(
        enabled = enabled,
        apiKey = "dummy-api-key",
        senderName = "test-name",
        senderDomain = "test-domain-com",
        replyToEmail = "no-reply-dot-com",
    )
}
