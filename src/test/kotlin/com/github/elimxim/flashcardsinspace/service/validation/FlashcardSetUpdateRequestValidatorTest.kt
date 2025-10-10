package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime

@SpringBootTest
class FlashcardSetUpdateRequestValidatorTest {
    @Autowired
    private lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        // given:
        val request = validRequest()

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.name).isEqualTo("Updated Set Name")
        assertThat(validRequest.status).isEqualTo(FlashcardSetStatus.ACTIVE)
        assertThat(validRequest.languageId).isEqualTo(2L)
        assertThat(validRequest.startedAt).isEqualTo(ZonedDateTime.parse("2025-09-30T10:15:30+01:00[Europe/Paris]"))
    }

    @Test
    fun `should pass validation if optional fields are null`() {
        // given:
        val request = validRequest().apply {
            startedAt = null
        }

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.startedAt).isNull()
    }

    @Test
    fun `should fail validation if name is too long`() {
        // given:
        val request = validRequest().apply {
            name = "a".repeat(65)
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request) }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name contains invalid characters`() {
        // given:
        val request = validRequest().apply {
            name = "Invalid!"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request) }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if status is invalid`() {
        // given:
        val request = validRequest().apply {
            status = "INVALID_STATUS"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request) }

        // then:
        assertThat(exception.fields).containsExactly("status")
    }

    @Test
    fun `should fail validation if languageId is not a number`() {
        // given:
        val request = validRequest().apply {
            languageId = "abc"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request) }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if startedAt has invalid format`() {
        // given:
        val request = validRequest().apply {
            startedAt = "2025-09-30 10:15:30"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request) }

        // then:
        assertThat(exception.fields).containsExactly("startedAt")
    }

    @Test
    fun `should fail validation if multiple fields are invalid`() {
        // given:
        val request = FlashcardSetUpdateRequest().apply {
            name = "!"
            status = "invalid"
            languageId = ""
            startedAt = "2025"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request) }

        // then:
        assertThat(exception.fields).containsExactlyInAnyOrder(
            "languageId",
            "name",
            "startedAt",
            "status"
        )
    }

    private fun validRequest(): FlashcardSetUpdateRequest {
        return FlashcardSetUpdateRequest().apply {
            name = "Updated Set Name"
            status = "ACTIVE"
            languageId = "2"
            startedAt = "2025-09-30T10:15:30+01:00[Europe/Paris]"
        }
    }
}
