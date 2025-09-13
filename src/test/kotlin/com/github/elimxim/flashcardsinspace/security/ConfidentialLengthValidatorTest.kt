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
        // given:
        val validationContainer = object {
            @ConfidentialLength(min = 0, max = 42)
            val secret: Secret? = null
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should not pass if the length is less than min`() {
        // given:
        val validationContainer = object {
            @ConfidentialLength(min = 10)
            val secret: Secret = Secret("0".repeat(8))
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isEqualTo("0".repeat(8))
    }

    @Test
    fun `should not pass if the length is grater than max`() {
        // given:
        val validationContainer = object {
            @ConfidentialLength(max = 10)
            val secret: Secret = Secret("0".repeat(12))
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isEqualTo("0".repeat(12))
    }

    @Test
    fun `should pass if the length is between min and max`() {
        // given:
        val validationContainer = object {
            @ConfidentialLength(min = 10, max = 20)
            val secret: Secret = Secret("0".repeat(15))
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).isEmpty()
    }
}
