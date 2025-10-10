package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.security.Password
import com.github.elimxim.flashcardsinspace.web.dto.LoginRequest
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
class LoginRequestValidatorTest {
    @Autowired
    lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        // given:
        val request = LoginRequest().apply {
            email = "test@example.com"
            secret = Password("password")
            timezone = "America/New_York"
        }

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.email).isEqualTo("test@example.com")
        assertThat(validRequest.secret.unmasked()).isEqualTo("password")
        assertThat(validRequest.timezone).isEqualTo("America/New_York")
    }

    @Test
    fun `should fail validation if email is null`() {
        // given:
        val request = LoginRequest().apply {
            email = null
            secret = Password("password")
            timezone = "UTC"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email is empty`() {
        // given:
        val request = LoginRequest().apply {
            email = ""
            secret = Password("password")
            timezone = "UTC"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email is blank`() {
        // given:
        val request = LoginRequest().apply {
            email = "  "
            secret = Password("password")
            timezone = "UTC"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email has invalid format`() {
        // given:
        val request = LoginRequest().apply {
            email = "invalid-email"
            secret = Password("password")
            timezone = "UTC"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if secret is null`() {
        // given:
        val request = LoginRequest().apply {
            email = "test@example.com"
            secret = null
            timezone = "UTC"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is empty`() {
        // given:
        val request = LoginRequest().apply {
            email = "test@example.com"
            secret = Password("")
            timezone = "UTC"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is blank`() {
        // given:
        val request = LoginRequest().apply {
            email = "test@example.com"
            secret = Password("  ")
            timezone = "UTC"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if timezone is null`() {
        // given:
        val request = LoginRequest().apply {
            email = "test@example.com"
            secret = Password("password")
            timezone = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is empty`() {
        // given:
        val request = LoginRequest().apply {
            email = "test@example.com"
            secret = Password("password")
            timezone = ""
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is blank`() {
        // given:
        val request = LoginRequest().apply {
            email = "test@example.com"
            secret = Password("password")
            timezone = "  "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is invalid`() {
        // given:
        val request = LoginRequest().apply {
            email = "test@example.com"
            secret = Password("password")
            timezone = "Invalid/Timezone"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(1)
        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should pass validation with different valid timezones`() {
        listOf("UTC", "America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney").forEach { tz ->
            // given:
            val request = LoginRequest().apply {
                email = "test@example.com"
                secret = Password("password")
                timezone = tz
            }

            // when:
            val validRequest = validator.validate(request)

            // then:
            assertThat(validRequest.timezone).isEqualTo(tz)
        }
    }

    @Test
    fun `should fail validation if all fields are invalid`() {
        // given:
        val request = LoginRequest().apply {
            email = "invalid"
            secret = Password("")
            timezone = "Invalid/Timezone"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).hasSize(3)
        assertThat(exception.fields).containsAll(listOf("email", "secret", "timezone"))
    }
}
