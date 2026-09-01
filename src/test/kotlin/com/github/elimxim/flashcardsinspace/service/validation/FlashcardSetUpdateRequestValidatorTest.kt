package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
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
        val request = validRequest()

        val validRequest = validator.validate(request)

        assertThat(validRequest.name).isEqualTo("Updated Set Name")
        assertThat(validRequest.status).isEqualTo(FlashcardSetStatus.ACTIVE)
        assertThat(validRequest.languageId).isEqualTo(2L)
        assertThat(validRequest.startedAt).isEqualTo(ZonedDateTime.parse("2025-09-30T10:15:30+01:00[Europe/Paris]"))
    }

    @Test
    fun `should pass validation if optional fields are null`() {
        val request = validRequest().apply {
            startedAt = null
        }

        val validRequest = validator.validate(request)

        assertThat(validRequest.startedAt).isNull()
    }

    @Test
    fun `should fail validation if name is too long`() {
        val request = validRequest().apply {
            name = "a".repeat(65)
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request) }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name contains invalid characters`() {
        val request = validRequest().apply {
            name = "Invalid!"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request) }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if status is invalid`() {
        val request = validRequest().apply {
            status = "INVALID_STATUS"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request) }

        assertThat(exception.fields).containsExactly("status")
    }

    @Test
    fun `should fail validation if languageId is not a number`() {
        val request = validRequest().apply {
            languageId = "abc"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request) }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if startedAt has invalid format`() {
        val request = validRequest().apply {
            startedAt = "2025-09-30 10:15:30"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request) }

        assertThat(exception.fields).containsExactly("startedAt")
    }

    @Test
    fun `should fail validation if multiple fields are invalid`() {
        val request = FlashcardSetUpdateRequest().apply {
            name = "!"
            status = "invalid"
            languageId = ""
            startedAt = "2025"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request) }

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
