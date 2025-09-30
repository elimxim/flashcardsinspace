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
        // given:
        val validationContainer = object {
            @RequiredConfidential
            val secret: Secret = Secret("1234")
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should be not empty`() {
        // given:
        val validationContainer = object {
            @RequiredConfidential
            val secret: Secret = Secret("")
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isEqualTo("")
    }

    @Test
    fun `should not pass validation if it's null`() {
        // given:
        val validationContainer = object {
            @RequiredConfidential
            val secret: Secret? = null
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isNull()

    }
}
