package com.github.elimxim.flashcardsinspace.security

import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class ConfidentialPatternValidatorTest {
    @Autowired
    lateinit var validator: Validator

    @Test
    fun `should pass validation if it's null`() {
        val validationContainer = object {
            @ConfidentialPattern(regexp = PASSWORD_REGEX)
            val secret: Secret? = null
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should pass if every printable ASCII char is used`() {
        val validationContainer = object {
            @ConfidentialPattern(regexp = PASSWORD_REGEX)
            val secret: Secret = Secret((0x20..0x7E).map { it.toChar() }.joinToString(""))
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["пароль123", "pässword", "pass–word", "pass word", "pass\tword", "pass🚀word"])
    fun `should not pass if a char is outside printable ASCII`(value: String) {
        val validationContainer = object {
            @ConfidentialPattern(regexp = PASSWORD_REGEX)
            val secret: Secret = Secret(value)
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).hasSize(1)
        assertThat(violations.first().propertyPath.toString()).isEqualTo("secret")
    }
}
