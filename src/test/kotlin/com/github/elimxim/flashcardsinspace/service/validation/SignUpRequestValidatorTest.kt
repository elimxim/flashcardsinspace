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
        // given:
        val request = validRequest()

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.email).isEqualTo("test@example.com")
        assertThat(validRequest.name).isEqualTo("Te_st Us-er")
        assertThat(validRequest.secret.unmasked()).isEqualTo("password123")
        assertThat(validRequest.languageId).isEqualTo(1L)
        assertThat(validRequest.timezone).isEqualTo("America/New_York")
    }

    @Test
    fun `should fail validation if email is null`() {
        // given:
        val request = validRequest().apply {
            email = null
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email is empty`() {
        // given:
        val request = validRequest().apply {
            email = ""
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email is blank`() {
        // given:
        val request = validRequest().apply {
            email = "  "
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if email has invalid format`() {
        // given:
        val request = validRequest().apply {
            email = "invalid-email"
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("email")
    }

    @Test
    fun `should fail validation if name is null`() {
        // given:
        val request = validRequest().apply {
            name = null
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is empty`() {
        // given:
        val request = validRequest().apply {
            name = ""
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is blank`() {
        // given:
        val request = validRequest().apply {
            name = "  "
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is too long`() {
        // given:
        val request = validRequest().apply {
            name = "a".repeat(65)
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name contains invalid characters`() {
        // given:
        val request = validRequest().apply {
            name = "invalid name@"
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if secret is null`() {
        // given:
        val request = validRequest().apply {
            secret = null
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is empty`() {
        // given:
        val request = validRequest().apply {
            secret = Password("")
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is blank`() {
        // given:
        val request = validRequest().apply {
            secret = Password("   ")
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is too short`() {
        // given:
        val request = validRequest().apply {
            secret = Password("12345")
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if secret is too long`() {
        // given:
        val request = validRequest().apply {
            secret = Password("a".repeat(65))
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("secret")
    }

    @Test
    fun `should fail validation if languageId is null`() {
        // given:
        val request = validRequest().apply {
            languageId = null
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is empty`() {
        // given:
        val request = validRequest().apply {
            languageId = ""
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is blank`() {
        // given:
        val request = validRequest().apply {
            languageId = "  "
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is not a number`() {
        // given:
        val request = validRequest().apply {
            languageId = "abc"
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if timezone is null`() {
        // given:
        val request = validRequest().apply {
            timezone = null
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is empty`() {
        // given:
        val request = validRequest().apply {
            timezone = ""
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is blank`() {
        // given:
        val request = validRequest().apply {
            timezone = "  "
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should fail validation if timezone is invalid`() {
        // given:
        val request = validRequest().apply {
            timezone = "Invalid/Timezone"
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("timezone")
    }

    @Test
    fun `should pass validation with different valid timezones`() {
        listOf("UTC", "America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney").forEach { tz ->
            // given:
            val request = validRequest().apply {
                timezone = tz
            }

            // when:
            val validRequest = validator.validate(request)

            // then:
            assertThat(validRequest.timezone).isEqualTo(tz)
        }
    }

    @Test
    fun `should fail validation with all invalid fields`() {
        // given:
        val request = SignUpRequest().apply {
            email = "invalid-email"
            name = "a".repeat(65)
            secret = Password("123")
            languageId = "abc"
            timezone = "Invalid/Timezone"
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
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
