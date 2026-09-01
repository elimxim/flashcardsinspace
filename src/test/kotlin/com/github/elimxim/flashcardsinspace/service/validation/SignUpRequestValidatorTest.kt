package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.security.Password
import com.github.elimxim.flashcardsinspace.web.dto.SignUpRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SignUpRequestValidatorTest {
    @Autowired
    lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        val request = validRequest()

        val validRequest = validator.validate(request)

        assertThat(validRequest.email).isEqualTo("test@example.com")
        assertThat(validRequest.name).isEqualTo("Te_st Us-er")
        assertThat(validRequest.secret.unmasked()).isEqualTo("password123")
        assertThat(validRequest.languageId).isEqualTo(1L)
        assertThat(validRequest.timezone).isEqualTo("America/New_York")
    }

    @Test
    fun `should fail validation if email is null`() {
        val request = validRequest().apply {
            email = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email is empty`() {
        val request = validRequest().apply {
            email = ""
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email is blank`() {
        val request = validRequest().apply {
            email = "  "
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email has invalid format`() {
        val request = validRequest().apply {
            email = "invalid-email"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if name is null`() {
        val request = validRequest().apply {
            name = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is empty`() {
        val request = validRequest().apply {
            name = ""
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is blank`() {
        val request = validRequest().apply {
            name = "  "
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is too long`() {
        val request = validRequest().apply {
            name = "a".repeat(65)
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name contains invalid characters`() {
        val request = validRequest().apply {
            name = "invalid name@"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if secret is null`() {
        val request = validRequest().apply {
            secret = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is empty`() {
        val request = validRequest().apply {
            secret = Password("")
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is blank`() {
        val request = validRequest().apply {
            secret = Password("   ")
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is too short`() {
        val request = validRequest().apply {
            secret = Password("12345")
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is too long`() {
        val request = validRequest().apply {
            secret = Password("a".repeat(65))
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if languageId is null`() {
        val request = validRequest().apply {
            languageId = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is empty`() {
        val request = validRequest().apply {
            languageId = ""
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is blank`() {
        val request = validRequest().apply {
            languageId = "  "
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is not a number`() {
        val request = validRequest().apply {
            languageId = "abc"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if timezone is null`() {
        val request = validRequest().apply {
            timezone = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is empty`() {
        val request = validRequest().apply {
            timezone = ""
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is blank`() {
        val request = validRequest().apply {
            timezone = "  "
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is invalid`() {
        val request = validRequest().apply {
            timezone = "Invalid/Timezone"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should pass validation with different valid timezones`() {
        listOf("UTC", "America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney").forEach { tz ->
            val request = validRequest().apply {
                timezone = tz
            }

            val validRequest = validator.validate(request)

            assertThat(validRequest.timezone).isEqualTo(tz)
        }
    }

    @Test
    fun `should fail validation with all invalid fields`() {
        val request = SignUpRequest().apply {
            email = "invalid-email"
            name = "a".repeat(65)
            secret = Password("123")
            languageId = "abc"
            timezone = "Invalid/Timezone"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("email", "languageId", "name", "secret", "timezone")
    }

    private fun validRequest() = SignUpRequest().apply {
        email = "test@example.com"
        name = "Te_st Us-er"
        secret = Password("password123")
        languageId = "1"
        timezone = "America/New_York"
    }
}
