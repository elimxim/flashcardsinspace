package com.github.elimxim.flashcardsinspace.security

import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class SecretValidatorTest {
    @Autowired
    lateinit var validator: Validator

    @Test
    fun `Secret should pass validation if it's valid`() {
        // given:
        val validationContainer = object {
            @ValidConfidential
            val secret: Secret = Secret("1234")
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).isEmpty()
    }

    @Test
    fun `Secret should be not empty`() {
        // given:
        val validationContainer = object {
            @ValidConfidential
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
    fun `Secret should have length between 0 and 64`() {
        // given:
        val validationContainer = object {
            @ValidConfidential
            val secret: Secret = Secret("0".repeat(65))
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).hasSize(1)

        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("secret")
        assertThat(violation.invalidValue).isEqualTo("0".repeat(65))
    }

    @Test
    fun `Secret should pass validation if it's null`() {
        // given:
        val validationContainer = object {
            @ValidConfidential
            val secret: Secret? = null
        }

        // when:
        val violations = validator.validate(validationContainer)

        // then:
        assertThat(violations).isEmpty()
    }
}
