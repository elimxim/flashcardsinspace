package com.github.elimxim.flashcardsinspace.security

import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class ConfidentialLengthValidatorTest {
    @Autowired
    lateinit var validator: Validator

    @Test
    fun `should pass validation if it's null or blank`() {
        val validationContainer = object {
            @ConfidentialLength(min = 0, max = 42)
            val secret: Secret? = null
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should not pass if the length is less than min`() {
        val validationContainer = object {
            @ConfidentialLength(min = 10)
            val secret: Secret = Secret("0".repeat(8))
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isEqualTo("0".repeat(8))
    }

    @Test
    fun `should not pass if the length is grater than max`() {
        val validationContainer = object {
            @ConfidentialLength(max = 10)
            val secret: Secret = Secret("0".repeat(12))
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isEqualTo("0".repeat(12))
    }

    @Test
    fun `should pass if the length is between min and max`() {
        val validationContainer = object {
            @ConfidentialLength(min = 10, max = 20)
            val secret: Secret = Secret("0".repeat(15))
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).isEmpty()
    }
}
