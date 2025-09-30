package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.ChronodayStatus
import com.github.elimxim.flashcardsinspace.web.dto.ChronoBulkUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChronoBulkUpdateRequestValidatorTest {
    @Autowired
    private lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        // given:
        val request = validRequest()

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.ids).containsExactly(1L, 2L, 3L)
        assertThat(validRequest.status).isEqualTo(ChronodayStatus.IN_PROGRESS)
    }

    @Test
    fun `should fail validation if ids is empty`() {
        // given:
        val request = validRequest().apply {
            ids = emptyList()
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("ids")
    }

    @Test
    fun `should fail validation if ids contains non-numeric value`() {
        // given:
        val request = validRequest().apply {
            ids = listOf(
                ChronoBulkUpdateRequest.ChronodayId("1"),
                ChronoBulkUpdateRequest.ChronodayId("abc"),
                ChronoBulkUpdateRequest.ChronodayId("3"),
            )
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("id")
    }

    @Test
    fun `should fail validation if status is null`() {
        // given:
        val request = validRequest().apply {
            status = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("status")
    }

    @Test
    fun `should fail validation if status is blank`() {
        // given:
        val request = validRequest().apply {
            status = " "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("status")
    }

    @Test
    fun `should fail validation if status is invalid`() {
        // given:
        val request = validRequest().apply {
            status = "INVALID_STATUS"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("status")
    }

    @Test
    fun `should fail validation if all fields are invalid`() {
        // given:
        val request = validRequest().apply {
            ids = listOf(
                ChronoBulkUpdateRequest.ChronodayId("a")
            )
            status = ""
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactlyInAnyOrder("id", "status")
    }

    private fun validRequest(): ChronoBulkUpdateRequest {
        return ChronoBulkUpdateRequest().apply {
            ids = listOf(
                ChronoBulkUpdateRequest.ChronodayId("1"),
                ChronoBulkUpdateRequest.ChronodayId("2"),
                ChronoBulkUpdateRequest.ChronodayId("3"),
            )
            status = "IN_PROGRESS"
        }
    }
}
