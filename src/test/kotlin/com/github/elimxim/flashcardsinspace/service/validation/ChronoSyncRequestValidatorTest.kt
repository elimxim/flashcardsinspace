package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.web.dto.ChronoSyncRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime

@SpringBootTest
class ChronoSyncRequestValidatorTest {
    @Autowired
    private lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        // given:
        val request = validRequest()

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.clientDatetime).isEqualTo(ZonedDateTime.parse("2025-09-30T10:15:30+01:00[Europe/Paris]"))
    }

    @Test
    fun `should fail validation if clientDatetime is null`() {
        // given:
        val request = ChronoSyncRequest().apply {
            clientDatetime = null
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("clientDatetime")
    }

    @Test
    fun `should fail validation if clientDatetime is empty`() {
        // given:
        val request = ChronoSyncRequest().apply {
            clientDatetime = ""
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("clientDatetime")
    }

    @Test
    fun `should fail validation if clientDatetime is blank`() {
        // given:
        val request = ChronoSyncRequest().apply {
            clientDatetime = "   "
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("clientDatetime")
    }

    @Test
    fun `should fail validation if clientDatetime has invalid format`() {
        // given:
        val request = ChronoSyncRequest().apply {
            clientDatetime = "2025-09-30 10:15:30"
        }

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("clientDatetime")
    }

    private fun validRequest(): ChronoSyncRequest {
        return ChronoSyncRequest().apply {
            clientDatetime = "2025-09-30T10:15:30+01:00[Europe/Paris]"
        }
    }
}
