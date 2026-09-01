package com.github.elimxim.flashcardsinspace.security

import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
class RequiredConfidentialValidatorTest {
    @Autowired
    lateinit var validator: Validator

    @Test
    fun `should pass validation if it's valid`() {
        val validationContainer = object {
            @RequiredConfidential
            val secret: Secret = Secret("1234")
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should be not empty`() {
        val validationContainer = object {
            @RequiredConfidential
            val secret: Secret = Secret("")
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isEqualTo("")
    }

    @Test
    fun `should not pass validation if it's null`() {
        val validationContainer = object {
            @RequiredConfidential
            val secret: Secret? = null
        }

        val violations = validator.validate(validationContainer)

        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isNull()

    }
}
