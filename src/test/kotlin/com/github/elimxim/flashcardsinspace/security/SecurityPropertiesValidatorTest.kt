package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.AppConfig
import com.github.elimxim.flashcardsinspace.config.SecurityPropertiesValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.validation.BindValidationException
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.mock.env.MockEnvironment
import org.springframework.validation.BeanPropertyBindingResult

class SecurityPropertiesValidatorTest {
    @EnableConfigurationProperties(SecurityProperties::class)
    class SecurityPropertiesConfig

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(AppConfig::class.java, SecurityPropertiesConfig::class.java)
        .withPropertyValues(
            "app.mail.enabled=true",
            "app.mail.api-key=dummy-api-key",
            "app.mail.sender-name=test-name",
            "app.mail.sender-domain=test-domain-com",
            "app.mail.reply-to-email=no-reply-dot-com",
            "app.security.jwt.secret=dummy-secret",
            "app.security.jwt.access-token-expiration-ms=1000",
            "app.security.jwt.refresh-token-expiration-ms=2000",
            "app.security.verification-tokens.registration-request.length=4",
            "app.security.verification-tokens.registration-request.max-age=60",
            "app.security.verification-tokens.password-reset-request.length=4",
            "app.security.verification-tokens.password-reset-request.max-age=60",
            "app.security.verification-tokens.password-reset-access.length=4",
            "app.security.verification-tokens.password-reset-access.max-age=60",
        )

    @Test
    fun `should pass if cookies are secure without the dev profile`() {
        val errors = validate(securityProperties(secureCookies = true), MockEnvironment())

        assertThat(errors.hasErrors()).isFalse()
    }

    @Test
    fun `should pass if cookies are not secure with the dev profile`() {
        val environment = MockEnvironment().apply { setActiveProfiles("docker", "dev") }

        val errors = validate(securityProperties(secureCookies = false), environment)

        assertThat(errors.hasErrors()).isFalse()
    }

    @Test
    fun `should reject secureCookies if cookies are not secure without the dev profile`() {
        val environment = MockEnvironment().apply { setActiveProfiles("docker") }

        val errors = validate(securityProperties(secureCookies = false), environment)

        assertThat(errors.fieldErrors.map { it.field }).containsExactly("secureCookies")
    }

    @Test
    fun `should pass if security is disabled with the dev profile`() {
        val environment = MockEnvironment().apply { setActiveProfiles("dev") }

        val errors = validate(securityProperties(enabled = false), environment)

        assertThat(errors.hasErrors()).isFalse()
    }

    @Test
    fun `should pass if security is disabled with the test profile`() {
        val environment = MockEnvironment().apply { setActiveProfiles("test") }

        val errors = validate(securityProperties(enabled = false), environment)

        assertThat(errors.hasErrors()).isFalse()
    }

    @Test
    fun `should reject enabled if security is disabled without the dev or test profile`() {
        val environment = MockEnvironment().apply { setActiveProfiles("docker") }

        val errors = validate(securityProperties(enabled = false), environment)

        assertThat(errors.fieldErrors.map { it.field }).containsExactly("enabled")
    }

    @Test
    fun `should fail startup naming the property if security is disabled without the dev profile`() {
        contextRunner
            .withPropertyValues("app.security.enabled=false")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).rootCause()
                    .isInstanceOf(BindValidationException::class.java)
                    .hasMessageContaining("app.security.enabled")
            }
    }

    @Test
    fun `should default to secure cookies`() {
        contextRunner.run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context.getBean(SecurityProperties::class.java).secureCookies).isTrue()
        }
    }

    @Test
    fun `should fail startup naming the property if cookies are not secure without the dev profile`() {
        contextRunner
            .withPropertyValues("app.security.secure-cookies=false")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).rootCause()
                    .isInstanceOf(BindValidationException::class.java)
                    .hasMessageContaining("app.security.secure-cookies")
            }
    }

    @Test
    fun `should start if cookies are not secure with the dev profile`() {
        contextRunner
            .withPropertyValues("app.security.secure-cookies=false", "spring.profiles.active=dev")
            .run { context ->
                assertThat(context).hasNotFailed()
            }
    }

    private fun validate(properties: SecurityProperties, environment: MockEnvironment) =
        BeanPropertyBindingResult(properties, "app.security").also {
            SecurityPropertiesValidator(environment).validate(properties, it)
        }

    private fun securityProperties(enabled: Boolean = true, secureCookies: Boolean = true) = SecurityProperties(
        enabled = enabled,
        secureCookies = secureCookies,
        jwt = JwtProperties(secret = "dummy-secret", accessTokenExpirationMs = 1000, refreshTokenExpirationMs = 2000),
        verificationTokens = VerificationTokenProperties(
            registrationRequest = SecurityTokenProperties(length = 4, maxAge = 60),
            passwordResetRequest = SecurityTokenProperties(length = 4, maxAge = 60),
            passwordResetAccess = SecurityTokenProperties(length = 4, maxAge = 60),
        ),
    )
}
